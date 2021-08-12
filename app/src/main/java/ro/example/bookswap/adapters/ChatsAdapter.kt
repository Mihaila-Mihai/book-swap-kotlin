package ro.example.bookswap.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import ro.example.bookswap.MessageActivity
import ro.example.bookswap.R
import ro.example.bookswap.models.User

class ChatsAdapter(
    private val items: ArrayList<String>,
    val context: Context
): RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder>() {
    class ChatsViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView){
        val userImageView = itemView.findViewById<CircleImageView>(R.id.user_image_chats)
        val usernameView = itemView.findViewById<TextView>(R.id.chats_username)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder {
        return ChatsViewHolder(
            LayoutInflater.from(context).inflate(R.layout.chat_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {
        val ref = Firebase.database.reference.child("users").child(items[position])

        ref.get().addOnSuccessListener {
            val user = it.getValue<User>()!!
            Picasso.get().load(user.imageUrl).into(holder.userImageView)
            holder.usernameView.text = user.username
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, MessageActivity::class.java)
            intent.putExtra("user", items[position])
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}