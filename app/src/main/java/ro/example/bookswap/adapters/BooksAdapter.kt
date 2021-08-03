package ro.example.bookswap.adapters

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso
import ro.example.bookswap.BookVisualisationActivity
import ro.example.bookswap.R
import ro.example.bookswap.models.RequestModel
import java.io.InputStream


class BooksAdapter(
    private val items: ArrayList<RequestModel.Items>,
    private val context: Context,
    private val activity: Activity
) :
    RecyclerView.Adapter<BooksAdapter.BookViewHolder>() {


    class BookViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView = itemView.findViewById<TextView>(R.id.title_of_the_book) as TextView
        val authors: TextView = itemView.findViewById(R.id.authors_of_the_book) as TextView
        val thumbnail: ImageView = itemView.findViewById(R.id.thumbnail) as ImageView

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        Log.d("InCreateviewHolder", "Created")
        return BookViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.book_item, parent, false)
        )
    }


    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.textView.text = items[position].volumeInfo.title

        if (items[position].volumeInfo.imageLinks != null) {
            val img: String = items[position].volumeInfo.imageLinks.thumbnail
            val imgArray: List<String> = img.split(":")
            val newImgUri = "https:" + imgArray[1]
            Picasso.get().load(newImgUri)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.drawable.error)
                .into(holder.thumbnail)//
        }

        if (items[position].volumeInfo.authors != null) {
            holder.authors.text = items[position].volumeInfo.authors[0]
        }


        holder.itemView.setOnClickListener {

//            Toast.makeText(context, items[position].volumeInfo.title, Toast.LENGTH_SHORT).show()
//            val intent = Intent(context, BookVisualisationActivity::class.java)
////            intent.putExtra("titlu", items[position].volumeInfo.title)
//
//            val activityOptions: ActivityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
//                activity,
//                Pair(holder.textView, BookVisualisationActivity::VIEW_NAME_HEADER_TITLE.toString())
//            )
//
//            ActivityCompat.startActivity(context, intent, activityOptions.toBundle())
            val img = holder.thumbnail

            val intent = Intent(context, BookVisualisationActivity::class.java)
            val bundle =
                ActivityOptions.makeSceneTransitionAnimation(activity, img, "thumbnail").toBundle()
            if (items[position].volumeInfo.imageLinks != null) {
                intent.putExtra("thumbnail", items[position].volumeInfo.imageLinks.thumbnail)
            } else {
                intent.putExtra("thumbnail", "default")
            }
            context.startActivity(intent, bundle)

        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

}