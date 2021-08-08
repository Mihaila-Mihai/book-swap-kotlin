package ro.example.bookswap.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import ro.example.bookswap.R
import ro.example.bookswap.models.Book
import ro.example.bookswap.models.Like
import ro.example.bookswap.models.User

class CardStackAdapter(
    private val items: ArrayList<Book>,
    val context: Context
) : RecyclerView.Adapter<CardStackAdapter.CardStackViewHolder>() {

    private val reference: DatabaseReference = Firebase.database.reference
    private lateinit var owner: User
    private lateinit var viewHolder: CardStackAdapter.CardStackViewHolder


    class CardStackViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleView: TextView = itemView.findViewById(R.id.book_title_swap_view)
        val authorsView: TextView = itemView.findViewById(R.id.book_authors_swap_view)
        val thumbnailImage: ImageView = itemView.findViewById(R.id.thumbnail_image_swipe_view)
        val userImage: CircleImageView = itemView.findViewById(R.id.user_image_like)
        val usernameView: TextView = itemView.findViewById(R.id.username_swipe_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardStackViewHolder {
        return CardStackViewHolder(
            LayoutInflater.from(context).inflate(R.layout.swipe_card_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CardStackViewHolder, position: Int) {
        viewHolder = holder
        holder.titleView.text = items[position].title
        holder.authorsView.text = items[position].authors
        val imgArray: List<String> = items[position].thumbnail.split(":")
        val newImgUri = "https:" + imgArray[1]
        Picasso.get().load(newImgUri).fit().centerCrop().into(holder.thumbnailImage)

        val userReference = reference.child("users").child(items[position].owner)

        userReference.get().addOnSuccessListener {
            owner = it.getValue<User>()!!
            holder.usernameView.text = owner.username
            Picasso.get().load(owner.imageUrl).fit().centerCrop().into(holder.userImage)
        }

        holder.itemView.setOnClickListener { onItemClicked() }
    }

    fun onSwipe(position: Int) {
        val likes: MutableSet<String> = HashSet()
        var userAlreadyLikes: Boolean = false
        val ref = Firebase.database.reference.child("likes").child(owner.id)
        ref.get().addOnSuccessListener {
            for (el in it.children) {
                val like = el.getValue<Like>()
                if (like?.bookId == items[position].id) {
                    userAlreadyLikes = true
                    break
                }
            }

            Log.d("true/false", userAlreadyLikes.toString())
            if (!userAlreadyLikes) {
                ref.push().setValue(
                    Like(Firebase.auth.currentUser?.uid!!, items[position].id)
//                    Firebase.auth.currentUser?.uid + "`" + items[position].title + "`" + items[position].id
                )
            }
        }



//        likes.add(Firebase.auth.currentUser.toString())
    }

    private fun onItemClicked() {
        Toast.makeText(context, "Clicked", Toast.LENGTH_SHORT).show()
    }

    override fun getItemCount(): Int {
        return items.size
    }
}