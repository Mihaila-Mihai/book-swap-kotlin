package ro.example.bookswap

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_image_view.*

class ImageViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)

        val savedUri = Uri.parse(intent.getStringExtra("imageUri"))

        imageView.setImageURI(savedUri)
        decline.setOnClickListener { finish() }
    }
}