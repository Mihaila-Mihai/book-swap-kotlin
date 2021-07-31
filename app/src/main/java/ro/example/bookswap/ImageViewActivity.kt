package ro.example.bookswap

import android.content.ContentValues.TAG
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_image_view.*


class ImageViewActivity : AppCompatActivity() {

    private val storage = Firebase.storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)

        val savedUri = Uri.parse(intent.getStringExtra("imageUri"))

//        val byteArray = convertImageToByte(savedUri)

//        val image: Bitmap = BitmapFactory.decodeByteArray(byteArray, 100, 100)
        imageView.setImageURI(savedUri)
//        imageView.setImageBitmap(image)
        decline.setOnClickListener { finish() }
        accept.setOnClickListener { postPhoto(savedUri) }
    }

    private fun postPhoto(savedUri: Uri?) {
        val storageRef = storage.reference
        val imageRef: StorageReference? = Firebase.auth.currentUser?.let {
            storageRef.child("profile-images").child(it.uid)
                .child((System.currentTimeMillis() / 1000).toString())
        }

        val uploadTask = savedUri?.let { imageRef?.putFile(it) }

        val urlTask = uploadTask?.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imageRef?.downloadUrl
        }?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                Log.d("download uri", downloadUri.toString())
                updateDatabase(downloadUri)
            } else {
                Log.e(TAG, "ImageUpload:failed", task.exception)
            }
        }

    }

    private fun updateDatabase(downloadUri: Uri?) {
        val currentUser = Firebase.auth.currentUser!!
        val database = Firebase.database.reference

        val update = database.child("users").child(currentUser.uid).child("imageUrl")
            .setValue(downloadUri.toString())

        update.addOnSuccessListener {
            Toast.makeText(this, "Image changed", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

//    fun convertImageToByte(uri: Uri?): ByteArray? {
//        var data: ByteArray? = null
//        try {
//            val cr = baseContext.contentResolver
//            val inputStream = cr.openInputStream(uri!!)
//            val bitmap = BitmapFactory.decodeStream(inputStream)
//            val baos = ByteArrayOutputStream()
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
//            data = baos.toByteArray()
//        } catch (e: FileNotFoundException) {
//            e.printStackTrace()
//        }
//        return data
//    }

}