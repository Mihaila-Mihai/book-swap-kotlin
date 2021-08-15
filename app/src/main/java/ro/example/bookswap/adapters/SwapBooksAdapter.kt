package ro.example.bookswap.adapters

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.internal.service.Common
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import ro.example.bookswap.R
import ro.example.bookswap.interfaces.ItemClickListener
import ro.example.bookswap.models.Book
import ro.example.bookswap.models.Like
import ro.example.bookswap.singletons.SwapItems

class SwapBooksAdapter(
    private val items: ArrayList<Like>,
    val context: Context,
    val recycler: String
): RecyclerView.Adapter<SwapBooksAdapter.BookViewHolder>() {

    var rowIndex = -1

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        lateinit var clickListener: ItemClickListener

        fun setItemClickListener(itemClickListener: ItemClickListener) {
            this.clickListener = itemClickListener
        }


        val authors: TextView = itemView.findViewById(R.id.book_authors_recycler)
        val title: TextView = itemView.findViewById(R.id.book_title_recycler)
        val image: ImageView = itemView.findViewById(R.id.book_image)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            clickListener.onClick(view!!, adapterPosition)
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        return BookViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.swap_book_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val item: Like = items[position]

        Firebase.database.reference.child("books").child(item.bookId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val book: Book = snapshot.getValue<Book>()!!
//                Log.d("Books", "${book.thumbnail} ${book.title} ${book.authors}")
                val imgArray: List<String> = book.thumbnail.split(":")
                val newImgUri = "https:" + imgArray[1]
                Picasso.get().load(newImgUri).fit().centerCrop().into(holder.image)
                holder.title.text = book.title
                holder.authors.text = book.authors

                holder.setItemClickListener(object: ItemClickListener {
                    override fun onClick(view: View, position: Int) {
                        rowIndex = position

                        if (recycler == "user_books") {
                            SwapItems.userBook = book
                        } else {
                            SwapItems.myBook = book
                        }
                        notifyDataSetChanged()
                    }

                })

                if (rowIndex == position) {
                    holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.orange))
                } else {
                    holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


    }

    override fun getItemCount(): Int {
        return items.size
    }
}