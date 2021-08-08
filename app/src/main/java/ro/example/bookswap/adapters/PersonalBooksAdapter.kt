package ro.example.bookswap.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ro.example.bookswap.R
import ro.example.bookswap.models.Book

class PersonalBooksAdapter(
    private val items: ArrayList<Book>,
    val context: Context
) :
    RecyclerView.Adapter<PersonalBooksAdapter.BookViewHolder>() {


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
        Picasso.get().load(newImgUri).into(holder.thumbnailImage)

        holder.itemView.setOnClickListener { onItemClicked() }
    }

    private fun onItemClicked() {
        Toast.makeText(context, "Clicked", Toast.LENGTH_SHORT).show()
    }

    override fun getItemCount(): Int {
        return items.size
    }


}