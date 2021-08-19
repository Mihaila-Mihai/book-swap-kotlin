package ro.example.bookswap.adapters

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_sign_in.view.*
import ro.example.bookswap.BookVisualisationActivity
import ro.example.bookswap.R
import ro.example.bookswap.enums.Status
import ro.example.bookswap.models.Book
import ro.example.bookswap.models.Like
import ro.example.bookswap.models.Swap
import ro.example.bookswap.models.User
import javax.net.ssl.SSLEngineResult

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
        val acceptButton: AppCompatImageButton = itemView.findViewById(R.id.accept_button)
        val declineButton: AppCompatImageButton = itemView.findViewById(R.id.decline_button)
        val statusView: TextView = itemView.findViewById(R.id.status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SwapsViewHolder {
        return SwapsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.swap_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SwapsViewHolder, position: Int) {
        val swap: Swap = items[position]

        holder.statusView.text = swap.status.toString()

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


        if (swap.receiver != currentUser || swap.status != Status.IN_PROGRESS) {
            holder.acceptButton.visibility = View.GONE
            holder.declineButton.visibility = View.GONE
        }

        holder.acceptButton.setOnClickListener {
            holder.itemView.animate()
                .translationX(holder.itemView.width.toFloat())
                .alpha(0.0F)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        holder.itemView.visibility = View.GONE
                        Firebase.database.reference.child("swaps").child(swap.id).child("status")
                            .setValue(
                                Status.ACCEPTED
                            ).addOnSuccessListener {
                                Firebase.database.reference.child("books").child(swap.senderBook)
                                    .child("owner").setValue(currentUser).addOnSuccessListener {
                                        Firebase.database.reference.child("books")
                                            .child(swap.receiverBook)
                                            .child("owner").setValue(swap.sender)
                                            .addOnSuccessListener {
                                                Toast.makeText(
                                                    context,
                                                    "Swap success",
                                                    Toast.LENGTH_SHORT
                                                )
                                                    .show()
                                                Firebase.database.reference.child("swaps").get()
                                                    .addOnSuccessListener { result ->
                                                        for (el in result.children) {
                                                            val swapToVerify: Swap? =
                                                                el.getValue<Swap>()
                                                            if (swapToVerify?.status == Status.IN_PROGRESS) {
                                                                if (
                                                                    swapToVerify.receiverBook == swap.receiverBook ||
                                                                    swapToVerify.receiverBook == swap.senderBook ||
                                                                    swapToVerify.senderBook == swap.receiverBook ||
                                                                    swapToVerify.senderBook == swap.senderBook
                                                                ) {
                                                                    Firebase.database.reference.child(
                                                                        "swaps"
                                                                    ).child(swapToVerify.id)
                                                                        .child("status")
                                                                        .setValue(Status.DECLINED)
                                                                }
                                                            }
                                                        }
                                                    }

                                                // Delete likes if user who liked the book is the book's owner

                                                Firebase.database.reference.child("likes").child(swap.sender).get().addOnSuccessListener { res ->
                                                    for (el in res.children) {
                                                        val like: Like? = el.getValue<Like>()
                                                        Log.d("------------", "${el.key} ${like?.bookId} ${swap.senderBook} ${like?.userId} ${swap.receiver}")
                                                        if (like!!.bookId == swap.senderBook && like.userId == swap.receiver) {
                                                            Firebase.database.reference.child("likes").child(swap.sender).child(el.key!!).setValue(null)
                                                        }

                                                    }
                                                }

                                                Firebase.database.reference.child("likes").child(swap.receiver).get().addOnSuccessListener { res ->
                                                    for (el in res.children) {
                                                        val like: Like? = el.getValue<Like>()
                                                        if (like!!.bookId == swap.receiverBook && like.userId == swap.sender) {
                                                            Firebase.database.reference.child("likes").child(swap.receiver).child(el.key!!).setValue(null)
                                                        }

                                                    }
                                                }

                                                // Make a copy of the book

//                                                Firebase.database.reference.child("books").child(swap.receiverBook).get().addOnSuccessListener {
//                                                    val book: Book? = it.getValue<Book>()
//                                                    book?.owner = swap.sender
//                                                    val id = (System.currentTimeMillis() / 1000 + (1..100000).random()).toString()
//                                                    Firebase.database.reference.child("history").child(id)
//
//                                                }


                                            }
                                    }
                            }
                    }
                })
        }

        holder.declineButton.setOnClickListener {
            holder.itemView.animate()
                .translationX(-holder.itemView.width.toFloat())
                .alpha(0.0F)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        holder.itemView.visibility = View.GONE
                        Firebase.database.reference.child("swaps").child(swap.id).child("status")
                            .setValue(Status.DECLINED).addOnSuccessListener {

                                Firebase.database.reference.child("likes").child(swap.sender).get().addOnSuccessListener { res ->
                                    for (el in res.children) {
                                        val like: Like? = el.getValue<Like>()
                                        Log.d("------------", "${el.key} ${like?.bookId} ${swap.senderBook} ${like?.userId} ${swap.receiver}")
                                        if (like!!.bookId == swap.senderBook && like.userId == swap.receiver) {
                                            Firebase.database.reference.child("likes").child(swap.sender).child(el.key!!).setValue(null)
                                        }

                                    }
                                }

                                Firebase.database.reference.child("likes").child(swap.receiver).get().addOnSuccessListener { res ->
                                    for (el in res.children) {
                                        val like: Like? = el.getValue<Like>()
                                        if (like!!.bookId == swap.receiverBook && like.userId == swap.sender) {
                                            Firebase.database.reference.child("likes").child(swap.receiver).child(el.key!!).setValue(null)
                                        }

                                    }
                                }
                            }
                    }
                })
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