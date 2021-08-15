package ro.example.bookswap.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import ro.example.bookswap.R
import ro.example.bookswap.models.Book
import ro.example.bookswap.models.Swap
import ro.example.bookswap.models.User

class SwapsAdapter(
    private val items: ArrayList<Swap>,
    val context: Context
) : RecyclerView.Adapter<SwapsAdapter.SwapsViewHolder>() {

    val ref = Firebase.database.reference
    val currentUser = Firebase.auth.currentUser?.uid!!

    class SwapsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val myBookImage: ImageView = itemView.findViewById(R.id.my_book_image)
        val userBookImage: ImageView = itemView.findViewById(R.id.user_book_image)
        val userImage: ImageView = itemView.findViewById(R.id.user_image)
        val username: TextView = itemView.findViewById(R.id.username_swap_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SwapsViewHolder {
        return SwapsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.swap_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SwapsViewHolder, position: Int) {
        val swap: Swap = items[position]

        ref.child("users").child(swap.sender).get().addOnSuccessListener {
            val user: User = it.getValue<User>()!!
            if (user.id != currentUser) {
                Picasso.get().load(user.imageUrl).into(holder.userImage)
                holder.username.text = user.username
            } else {
                loadUserImage(swap, holder)
            }


        }

        ref.child("books").child(swap.senderBook).get().addOnSuccessListener {
            val book: Book = it.getValue<Book>()!!
            loadImage(book, holder)
        }

        ref.child("books").child(swap.receiverBook).get().addOnSuccessListener {
            val book: Book = it.getValue<Book>()!!
            loadImage(book, holder)
        }

    }

    private fun loadUserImage(swap: Swap, holder: SwapsViewHolder) {
        ref.child("users").child(swap.receiver).get().addOnSuccessListener {
            val user: User? = it.getValue<User>()
            Picasso.get().load(user?.imageUrl).into(holder.userImage)
            holder.username.text = user?.username
        }
    }

    private fun loadImage(book: Book, holder: SwapsViewHolder) {
        val imgArray: List<String> = book.thumbnail.split(":")
        val newImgUri = "https:" + imgArray[1]
        if (book.owner == currentUser) {
            Picasso.get().load(newImgUri).into(holder.myBookImage)
        } else {
            Picasso.get().load(newImgUri).into(holder.userBookImage)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}