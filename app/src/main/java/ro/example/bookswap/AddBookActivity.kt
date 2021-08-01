package ro.example.bookswap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_add_book.*
import ro.example.bookswap.interfaces.GoogleBooksApiService
import java.io.FileInputStream
import java.util.*
import kotlin.collections.ArrayList

class AddBookActivity : AppCompatActivity() {

    private var imageList = ArrayList<SlideModel>()
    private lateinit var regEx: Regex
    private lateinit var bookISBN: String
    private var disposable: Disposable? = null

    private val googleBooksApiService by lazy {
        GoogleBooksApiService.create()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)

        val searchString = intent.getStringExtra("searchString")
        val newSearchString = getSearchString(searchString)
        beginSearch(newSearchString)


//        add_photo_button.setOnClickListener { addBookDialog() }

//        val data = arrayOf("1", "2", "3")
//        number_of_books.minValue = 1
//        number_of_books.maxValue = 3
//        number_of_books.displayedValues = data
//
//        number_of_books.setOnValueChangedListener { numberPicker, i, i2 ->
//            run {
//                Toast.makeText(this, i.toString() + i2.toString() + numberPicker.toString(), Toast.LENGTH_SHORT).show()
//            }
//        }


        regEx = Regex(
            "^(?:ISBN(?:-1[03])?:?●)?(?=[0-9X]{10}\$|(?=(?:[0-9]+[-●]){3})" + "[-●0-9X]{13}\$|97[89][0-9]{10}\$|(?=(?:[0-9]+[-●]){4})[-●0-9]{17}\$)" + "(?:97[89][-●]?)?[0-9]{1,5}[-●]?[0-9]+[-●]?[0-9]+[-●]?[0-9X]\$"
        )
    }

    override fun onPause() {
        super.onPause()
        disposable?.dispose()
    }

    private fun beginSearch(newSearchString: String) {
        disposable = googleBooksApiService.getBooks(newSearchString, BuildConfig.apiKey)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result ->
                    run {
                        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                        book_title.setText(result.items[0].volumeInfo.title)
                    }
                },
                { error ->
                    run {
                        Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
                        Log.d("GoogleBooksApiError: ", error.message!!)
                    }
                }
            )
    }

    private fun getSearchString(searchString: String?): String {
        var newSearchString = ""
        val searchStringArray = searchString?.split("\n")
        for (el in searchStringArray!!) {
            var newSearchArray = el.split(" ")
            for (el in newSearchArray!!) {
                newSearchString += "${el.lowercase()}+"

            }
        }

        return newSearchString
    }


    private fun addBookDialog() {

        if (isbn.text.toString().isEmpty()) {
            Toast.makeText(this, "You have to give an ISBN first", Toast.LENGTH_SHORT).show()
        } else if (!regEx.matches(isbn.text.toString())) {
            Toast.makeText(this, "ISBN format not respected", Toast.LENGTH_SHORT).show()
        } else {
            bookISBN = isbn.text.toString()
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
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 204) {
            if (resultCode == RESULT_OK) {
                val uriArrayList = data?.getStringArrayListExtra("photoUris")

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
    }
}