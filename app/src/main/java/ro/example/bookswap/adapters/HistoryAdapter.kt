package ro.example.bookswap.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import ro.example.bookswap.BookVisualisationActivity
import ro.example.bookswap.ProfileVisualisationActivity
import ro.example.bookswap.R
import ro.example.bookswap.models.Book
import ro.example.bookswap.models.Swap
import ro.example.bookswap.models.User

class HistoryAdapter(
    private val items: ArrayList<Swap>,
    val context: Context
): RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private val currentUser = Firebase.auth.currentUser?.uid

    class HistoryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val receiverBook: ImageView = itemView.findViewById<ImageView>(R.id.receiver_book)
        val senderBook: ImageView = itemView.findViewById<ImageView>(R.id.sender_book)
        val receiverPhoto: CircleImageView = itemView.findViewById<CircleImageView>(R.id.receiver_photo)
        val senderPhoto: CircleImageView = itemView.findViewById<CircleImageView>(R.id.sender_photo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val swap: Swap = items[position]
        Firebase.database.reference.child("books").child(swap.senderBook).get().addOnSuccessListener {
            val book: Book? = it.getValue<Book>()
            val imgArray: List<String> = book?.thumbnail?.split(":")!!
            val newImgUri = "https:" + imgArray[1]
            Picasso.get().load(newImgUri).fit().centerCrop().into(holder.senderBook)
        }
        Firebase.database.reference.child("books").child(swap.receiverBook).get().addOnSuccessListener {
            val book: Book? = it.getValue<Book>()
            val imgArray: List<String> = book?.thumbnail?.split(":")!!
            val newImgUri = "https:" + imgArray[1]
            Picasso.get().load(newImgUri).fit().centerCrop().into(holder.receiverBook)
        }
        Firebase.database.reference.child("users").child(swap.sender).get().addOnSuccessListener {
            val user:User? = it.getValue<User>()
            Picasso.get().load(user?.imageUrl).into(holder.senderPhoto)
        }
        Firebase.database.reference.child("users").child(swap.receiver).get().addOnSuccessListener {
            val user:User? = it.getValue<User>()
            Picasso.get().load(user?.imageUrl).into(holder.receiverPhoto)
        }


        holder.receiverPhoto.setOnClickListener {
            if (swap.receiver != currentUser) {
                val intent = Intent(context, ProfileVisualisationActivity::class.java)
                intent.putExtra("userId", swap.receiver)
                context.startActivity(intent)
            }
        }

        holder.senderPhoto.setOnClickListener {
            if (swap.sender != currentUser) {
                val intent = Intent(context, ProfileVisualisationActivity::class.java)
                intent.putExtra("userId", swap.sender)
                context.startActivity(intent)
            }
        }

        holder.senderBook.setOnClickListener {
            if (swap.sender == currentUser) {
                val intent = Intent(context, BookVisualisationActivity::class.java)
                intent.putExtra("personal", "discover")
                intent.putExtra("id", swap.senderBook)
                context.startActivity(intent)
            } else {
                val intent = Intent(context, BookVisualisationActivity::class.java)
                intent.putExtra("personal", "discover")
                intent.putExtra("id", swap.senderBook)
                context.startActivity(intent)
            }
        }

        holder.receiverBook.setOnClickListener {
            if (swap.receiver == currentUser) {
                val intent = Intent(context, BookVisualisationActivity::class.java)
                intent.putExtra("personal", "discover")
                intent.putExtra("id", swap.senderBook)
                context.startActivity(intent)
            } else {
                val intent = Intent(context, BookVisualisationActivity::class.java)
                intent.putExtra("personal", "discover")
                intent.putExtra("id", swap.senderBook)
                context.startActivity(intent)
            }
        }

    }

    override fun getItemCount(): Int {
        return items.size
    }
}