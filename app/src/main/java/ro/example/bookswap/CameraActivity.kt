package ro.example.bookswap

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptions
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList


class CameraActivity : AppCompatActivity() {
    private var imageCapture: ImageCapture? = null

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var context: Context
    private lateinit var activity: String
    private var uriArrayList: ArrayList<String> = ArrayList()
    private val storage = Firebase.storage
    private val id = System.currentTimeMillis() / 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        activity = intent.getStringExtra("activity").toString()

        if (activity != "addBook") {
            finnish_button.visibility = View.GONE
        }

        context = this

        switch_button.setOnClickListener { switchCamera() }

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera(BACK_CAMERA)
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Set up the listener for take photo button
        camera_capture_button.setOnClickListener {
            Log.d("CameraActivity:", "camera button pressed")
            takePhoto()
        }

        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()

        finnish_button.setOnClickListener { returnResult() }
    }

    private fun returnResult() {
        val data = Intent()

        data.putStringArrayListExtra("photoUris", uriArrayList)

        setResult(Activity.RESULT_OK, data)

        finish()
    }

    private fun switchCamera() {
        startCamera(!BACK_CAMERA)
        BACK_CAMERA = !BACK_CAMERA
    }

    private fun takePhoto() {
        Log.d("CameraActivity:", "inside takePhoto function")
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
//        val imageCapture = ImageCapture.Builder().setTargetResolution(Size(1920, 1080)).build()
        // Set up image capture listener, which is triggered after photo has
        // been taken
//        imageCapture.takePicture(cameraExecutor, object: ImageCapture.OnImageCapturedCallback() {
//            override fun onCaptureSuccess(image: ImageProxy) {
//
//            }
//        })
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
//                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                    if (activity != "addBook" && activity != "profileFragment") {
                        val intent = Intent(context, ImageViewActivity::class.java)
                        intent.putExtra("imageUri", savedUri.toString())
                        startActivity(intent)
                    } else if (activity == "profileFragment") {

                        UCrop.of(savedUri, savedUri)
                            .start(this@CameraActivity)


                    } else {
                        postPhoto(savedUri)
                    }

                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            val image: InputImage = InputImage.fromFilePath(context, resultUri!!)


            val recognizer =
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val result = recognizer.process(image).addOnSuccessListener {
                Toast.makeText(context, it.text, Toast.LENGTH_SHORT).show()
                val intent = Intent(this, AddBookActivity::class.java)
                intent.putExtra("searchString", it.text)
                startActivity(intent)
                finish()

            }

        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
        }
    }


    private fun postPhoto(savedUri: Uri?) {
        val storageRef = storage.reference
        val imageRef: StorageReference? = Firebase.auth.currentUser?.let {
            storageRef.child("books-images").child(it.uid)
                .child((id + (100..10000).random()).toString())
        }

        val uploadTask = savedUri?.let { imageRef?.putFile(it) }

        val urlTask = uploadTask?.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imageRef?.downloadUrl!!
        }?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                Log.d("download uri", downloadUri.toString())
                Toast.makeText(this, "Photo uploaded", Toast.LENGTH_SHORT).show()
                uriArrayList.add(downloadUri.toString())
            } else {
                Log.e(ContentValues.TAG, "ImageUpload:failed", task.exception)
            }
        }
    }

    private fun startCamera(cameraBack: Boolean) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().setTargetResolution(Size(1920, 1080)).build()

            // Select back camera as a default
            val cameraSelector = when (cameraBack) {
                true -> CameraSelector.DEFAULT_BACK_CAMERA
                false -> CameraSelector.DEFAULT_FRONT_CAMERA
            }
//            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera(BACK_CAMERA)
            } else {
                Toast.makeText(this, "Permissions not granted by user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private var BACK_CAMERA = true
    }
}