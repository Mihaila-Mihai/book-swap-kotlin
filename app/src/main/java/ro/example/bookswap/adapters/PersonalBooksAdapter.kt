package ro.example.bookswap.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import ro.example.bookswap.BookVisualisationActivity
import ro.example.bookswap.R
import ro.example.bookswap.models.Book

class PersonalBooksAdapter(
    private val items: ArrayList<Book>,
    val context: Context
) :
    RecyclerView.Adapter<PersonalBooksAdapter.BookViewHolder>() {

    val currentUser = Firebase.auth.currentUser?.uid


    class BookViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleView: TextView = itemView.findViewById(R.id.book_title_swap_view)
        val authorsView: TextView = itemView.findViewById(R.id.authors)
        val thumbnailImage: ImageView = itemView.findViewById(R.id.thumbnail_personal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        return BookViewHolder(
            LayoutInflater.from(context).inflate(R.layout.personal_book_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.titleView.text = items[position].title
        holder.authorsView.text = items[position].authors
        val imgArray: List<String> = items[position].thumbnail.split(":")
        val newImgUri = "https:" + imgArray[1]
        Picasso.get().load(newImgUri).fit().centerCrop().into(holder.thumbnailImage)

        holder.itemView.setOnClickListener { onItemClicked(items[position]) }
    }

    private fun onItemClicked(book: Book) {
        val intent = Intent(context, BookVisualisationActivity::class.java)
//        Toast.makeText(context, (book.owner == currentUser).toString(), Toast.LENGTH_SHORT).show()
        if (book.owner == currentUser) {
            intent.putExtra("personal", "true")
//            Toast.makeText(context, "true", Toast.LENGTH_SHORT).show()
        } else {
//            Toast.makeText(context, "false", Toast.LENGTH_SHORT).show()
            intent.putExtra("personal", "notPersonal")
        }
        intent.putExtra("id", book.id)
        context.startActivity(intent)
    }

    override fun getItemCount(): Int {
        return items.size
    }


}