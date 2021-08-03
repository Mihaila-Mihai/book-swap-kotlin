package ro.example.bookswap


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.core.view.ViewCompat
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_book_visualisation.*

class BookVisualisationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_visualisation)

        val img = intent.getStringExtra("thumbnail")!!
        if (img != "default") {
            val imgArray: List<String> = img.split(":")
            val newImgUri = "https:" + imgArray[1]
            Picasso.get().load(newImgUri).into(thumbnail)
        } else {
            Picasso.get().load(R.mipmap.ic_launcher).into(thumbnail)
        }


    }
}