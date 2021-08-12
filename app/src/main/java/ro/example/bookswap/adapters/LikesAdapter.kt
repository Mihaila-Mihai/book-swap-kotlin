package ro.example.bookswap.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import ro.example.bookswap.R
import ro.example.bookswap.models.Book
import ro.example.bookswap.models.Like
import ro.example.bookswap.models.User

class LikesAdapter(
    private val items: ArrayList<Like>,
    val context: Context
) : RecyclerView.Adapter<LikesAdapter.LikesViewHolder>() {

    class LikesViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.like_text)
        val userImage: CircleImageView = itemView.findViewById(R.id.user_image_like)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikesViewHolder {
        return LikesViewHolder(
            LayoutInflater.from(context).inflate(R.layout.like_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: LikesViewHolder, position: Int) {
        var user: User?
        var book: Book?
        val userRef = Firebase.database.reference.child("users").child(items[position].userId)
        userRef.get().addOnSuccessListener {
            user = it.getValue<User>()
            val bookRef = Firebase.database.reference.child("books").child(items[position].bookId)
            bookRef.get().addOnSuccessListener { el ->
                book = el.getValue<Book>()
                holder.textView.text = String.format(context.resources.getString(R.string.like_message), user?.username, book?.title)
                Picasso.get().load(user?.imageUrl).into(holder.userImage)
            }
        }

        holder.itemView.setOnClickListener {
            Log.d("hello", "click")
        }

        holder.itemView.setOnLongClickListener {
            Log.d("hello", "longClick")
            true
        }


    }

    override fun getItemCount(): Int {
        return items.size
    }
}