package ro.example.bookswap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Slide
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_add_book.*

class AddBookActivity : AppCompatActivity() {

    private var imageList = ArrayList<SlideModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)

        add_photo_button.setOnClickListener { addBookDialog() }
    }

    private fun addBookDialog() {
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