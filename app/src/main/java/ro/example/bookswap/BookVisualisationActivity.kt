package ro.example.bookswap

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.activity_book_visualisation.*
import kotlinx.android.synthetic.main.activity_sign_in.view.*
import ro.example.bookswap.models.Book
import ro.example.bookswap.models.RequestModel
import java.io.ByteArrayOutputStream
import java.lang.Exception


class BookVisualisationActivity : AppCompatActivity() {

    private var imageList = ArrayList<SlideModel>()

    private var plusClicked: Int = 0

    private lateinit var img: String
    private lateinit var title: String
    private lateinit var authors: String
    private lateinit var description: String
    private lateinit var pageCount: String
    private lateinit var language: String
    private lateinit var bookId: String

    private lateinit var titleView: EditText
    private lateinit var authorsView: EditText
    private lateinit var descriptionView: EditText
    private lateinit var pageCountView: EditText
    private lateinit var languageView: EditText

    private var uriArrayList: java.util.ArrayList<String>? = null

    private var editClicked: Int = 0

    private val storage = Firebase.storage
    private val id = System.currentTimeMillis() / 1000

    private val database: DatabaseReference = Firebase.database.reference
    private val currentUser = Firebase.auth.currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_visualisation)

        img = intent.getStringExtra("thumbnail")!!
        title = intent.getStringExtra("title")!!
        authors = intent.getStringExtra("authors")!!
        description = intent.getStringExtra("description")!!
        pageCount = intent.getStringExtra("pageCount")!!
        language = intent.getStringExtra("language")!!
        bookId = intent.getStringExtra("id")!!


        initialisePage()
        setTexts()

        add_book_button_visual.setOnClickListener { addBookToDatabase() }

        add_photo_button.setOnClickListener { addBookDialog() }

        edit_content_button.setOnClickListener {
            if (editClicked % 2 == 1) {
                setTextViews(View.INVISIBLE)
                setEditTexts(View.VISIBLE)
                add_book_button_visual.alpha = 0.5F
                edit_content_button.setImageResource(R.drawable.ic_save)
            } else {
                setTextViews(View.VISIBLE)
                setEditTexts(View.INVISIBLE)
                setTexts()
                add_book_button_visual.alpha = 1F
                edit_content_button.setImageResource(R.drawable.ic_edit_2)
            }
            editClicked++
        }


    }

    private fun addBookToDatabase() {
        if (add_book_button_visual.alpha == 1F) {
            var uriArray = ""
            if (plusClicked > 0) {
                for (uri in uriArrayList!!) {
                    uriArray += "$uri,"
                }
            }
            val id = (System.currentTimeMillis() / 1000 + (1..100000).random()).toString()
            val newBook = Book(
                title = title_of_the_book_text_view.text.toString(),
                authors = authors_text_view.text.toString(),
                id = id,
                thumbnail = img,
                description = description_text_view.text.toString(),
                pageCount = page_count_text_view.text.toString(),
                language = language_text_view.text.toString(),
                owner = currentUser!!,
                imageSliderUris = uriArray
            )

            val bookReference = database.child("books").child(id).setValue(newBook)
            bookReference.addOnSuccessListener {
                Toast.makeText(this, "Book added!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun setTexts() {
        title_of_the_book_text_view.text = titleView.text
        authors_text_view.text = authorsView.text
        page_count_text_view.text = page_count_text.text
        language_text_view.text = language_text.text
        description_text_view.text = descriptionView.text
    }

    private fun setTextViews(type: Int) {
        title_of_the_book_text_view.visibility = type
        authors_text_view.visibility = type
        page_count_text_view.visibility = type
        language_text_view.visibility = type
        description_text_view.visibility = type
    }

    private fun setEditTexts(type: Int) {
        titleView.visibility = type
        authorsView.visibility = type
        page_count_text.visibility = type
        language_text.visibility = type
        descriptionView.visibility = type
    }

    private fun addBookDialog() {
        plusClicked++

        val dialog = MaterialAlertDialogBuilder(this)
        dialog.setView(R.layout.image_change_method_dialog)
        val dialogCreated = dialog.create()
        dialogCreated.show()

        // Camera button clicked
        dialogCreated.findViewById<ImageButton>(R.id.camera_button)?.setOnClickListener {
            dialogCreated.dismiss()
            val intent = Intent(this, CameraActivity::class.java)
            intent.putExtra("activity", "addBook")
            startActivityForResult(intent, 204)
        }

        dialogCreated.findViewById<ImageButton>(R.id.media)?.setOnClickListener {
            dialogCreated.dismiss()

            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, 203)


        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 204) {
            if (resultCode == RESULT_OK) {
                uriArrayList = data?.getStringArrayListExtra("photoUris")

                for (el in uriArrayList!!) {
                    imageList.add(SlideModel(el))
                    Log.d("Uri", el.toString())
                }
                image_slider.setImageList(imageList)

                image_slider.setItemClickListener(object : ItemClickListener {
                    override fun onItemSelected(position: Int) {
                        imageList.removeAt(position)
                        image_slider.setImageList(imageList)
                    }
                })
            }
        }

        if (requestCode == 203) {
            if (resultCode == RESULT_OK) {
                val imageUri: Uri? = data?.data
                val imageStream = contentResolver.openInputStream(imageUri!!)
                val selectedImage = BitmapFactory.decodeStream(imageStream)
                uploadImage(selectedImage)
            }
        }
    }


    private fun uploadImage(selectedImage: Bitmap?) {
        val storageRef = storage.reference
        val imageRef: StorageReference? = Firebase.auth.currentUser?.let {
            storageRef.child("books-images").child(it.uid)
                .child((id + (100..10000).random()).toString())
        }

        val baos = ByteArrayOutputStream()
        selectedImage?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = imageRef?.putBytes(data)

        val urlTask = uploadTask?.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imageRef.downloadUrl
        }?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                imageList.add(SlideModel(downloadUri.toString()))
                uriArrayList?.add(downloadUri.toString())
                image_slider.setImageList(imageList)
                image_slider.setItemClickListener(object : ItemClickListener {
                    override fun onItemSelected(position: Int) {
                        imageList.removeAt(position)
                        image_slider.setImageList(imageList)
                    }
                })
            } else {
                Log.e(ContentValues.TAG, "ImageUpload:failed", task.exception)
            }
        }
    }

    private fun initialisePage() {
        titleView = findViewById(R.id.title_of_the_book)
        authorsView = findViewById(R.id.authors)
        descriptionView = findViewById(R.id.description)
        pageCountView = findViewById(R.id.page_count_text)
        languageView = findViewById(R.id.language_text)


        titleView.setText(title)
        if (img != "default") {
            val imgArray: List<String> = img.split(":")
            val newImgUri = "https:" + imgArray[1]
            Picasso.get().load(newImgUri).into(thumbnail)
            Picasso.get().load(newImgUri).into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    alt_container.background =
                        BitmapDrawable(this@BookVisualisationActivity.resources, bitmap)
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {

                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {

                }

            })
            alt_container.background.alpha = 120
        } else {
            Picasso.get().load(R.mipmap.ic_launcher).into(thumbnail)
            thumbnail.background.alpha = 0
            Picasso.get().load(R.mipmap.ic_launcher).into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    alt_container.background =
                        BitmapDrawable(this@BookVisualisationActivity.resources, bitmap)
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {

                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {

                }

            })
        }
        if (authors != null && authors != "undefined") {
            authorsView.setText(authors)
        } else {
            authorsView.setText(resources.getString(R.string.authors_not_found))
        }

        if (description != null && description != "undefined") {
            descriptionView.setText(description)
        } else {
            descriptionView.setText(resources.getString(R.string.description_not_found))
        }

        if (pageCount != null && pageCount != "undefined") {
            pageCountView.setText(pageCount)
        } else {
            pageCountView.setText(resources.getString(R.string.page_count_not_found))
        }

        if (language != null && language != "undefined") {
            languageView.setText(language.uppercase())
        } else {
            languageView.setText(resources.getString(R.string.language_not_found))
        }

    }
}