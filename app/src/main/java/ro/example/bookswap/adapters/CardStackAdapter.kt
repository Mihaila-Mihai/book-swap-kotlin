package ro.example.bookswap.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import ro.example.bookswap.BookVisualisationActivity
import ro.example.bookswap.R
import ro.example.bookswap.models.Book
import ro.example.bookswap.models.Like
import ro.example.bookswap.models.Match
import ro.example.bookswap.models.User

class CardStackAdapter(
    private val items: ArrayList<Book>,
    val context: Context
) : RecyclerView.Adapter<CardStackAdapter.CardStackViewHolder>() {

    val currentUser = Firebase.auth.currentUser?.uid
    private var pos: Int = 0

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
        val book = items[position]
        pos++
        viewHolder = holder
        holder.titleView.text = items[position].title
        holder.authorsView.text = items[position].authors
        try {
            val imgArray: List<String> = items[position].thumbnail.split(":")
            val newImgUri = "https:" + imgArray[1]
            Picasso.get().load(newImgUri).fit().centerCrop().into(holder.thumbnailImage)
        } catch (e: Exception) {
            Picasso.get().load(R.mipmap.ic_launcher).into(holder.thumbnailImage)
        }


        val userReference = reference.child("users").child(items[position].owner)

        userReference.get().addOnSuccessListener {
            owner = it.getValue<User>()!!
            holder.usernameView.text = owner.username
            Picasso.get().load(owner.imageUrl).fit().centerCrop().into(holder.userImage)
        }

        holder.itemView.setOnClickListener {
//            Toast.makeText(context, position.toString() + book.id, Toast.LENGTH_SHORT).show()
            val intent = Intent(context, BookVisualisationActivity::class.java)
//        Toast.makeText(context, (book.owner == currentUser).toString(), Toast.LENGTH_SHORT).show()
            if (book.owner == currentUser) {
                intent.putExtra("personal", "true")
//            Toast.makeText(context, "true", Toast.LENGTH_SHORT).show()
            } else {
//            Toast.makeText(context, "false", Toast.LENGTH_SHORT).show()
                intent.putExtra("personal", "discover")
            }
            intent.putExtra("id", book.id)
            context.startActivity(intent)
        }
    }

    fun onSwipe(position: Int, books: ArrayList<Book>) {
        Firebase.database.reference.child("users").child(books[position].owner).get().addOnSuccessListener { res ->
            val bookOwner = res.getValue<User>()!!
            val likes: MutableSet<String> = HashSet()
            var userAlreadyLikes: Boolean = false
            Log.d("owner", bookOwner.id)
            val ref = Firebase.database.reference.child("likes").child(bookOwner.id)
            ref.get().addOnSuccessListener {
                for (el in it.children) {
                    val like = el.getValue<Like>()
                    if (like?.bookId == books[position].id && like.userId == currentUser) {
                        userAlreadyLikes = true
                        break
                    }
                }



                Log.d("true/false", userAlreadyLikes.toString())
                Log.d("ref:", ref.toString())
                if (!userAlreadyLikes || !res.exists()) {
                    ref.push().setValue(
                        Like(Firebase.auth.currentUser?.uid!!, books[position].id)
//                    Firebase.auth.currentUser?.uid + "`" + items[position].title + "`" + items[position].id
                    )
                    verifyMatch(books[position].owner)
                }

            }
        }


//        likes.add(Firebase.auth.currentUser.toString())
    }

    private fun verifyMatch(owner: String) {
        var alreadyMatch = false
        val currentUser: String = Firebase.auth.currentUser?.uid!!
        val reference = Firebase.database.reference.child("likes").child(currentUser)
        reference.get().addOnSuccessListener {
            for (elem in it.children) {
                // TODO verific daca pe referinta apare o carte cu like de la owner catre mine - daca da inseamna match, daca nu aia e
                val likeElem = elem.getValue<Like>()
                if (likeElem?.userId == owner) {
                    // TODO verific daca nu au facut deja match
                    val matchRef = Firebase.database.reference.child("matches")
                    matchRef.get().addOnSuccessListener { matches ->
                        for (match in matches.children) {
                            val matchElem = match.getValue<Match>()
                            if ((matchElem?.userId1 == owner && matchElem.userId2 == currentUser) || (matchElem?.userId1 == currentUser && matchElem.userId2 == owner)) {
                                alreadyMatch = true
                            }
                        }

                        if (!alreadyMatch) {
                            matchRef.push().setValue(
                                Match(currentUser, owner)
                            )
                        }
                    }
                }
            }
        }

//        var alreadyMatches: Boolean = false
//        val currentUser: String = Firebase.auth.currentUser?.uid!!
//        val reference = Firebase.database.reference.child("matches")
//        reference.get().addOnSuccessListener {
//            for (elem in it.children) {
//                val match = elem.getValue<Match>()
//                if ((match?.userId1 == currentUser && match.userId2 == owner) || (match?.userId2 == currentUser && match.userId1 == owner)) {
//                    Log.d("match", match.toString())
//                    alreadyMatches = true
//                    break
//                }
//            }
//            Log.d("reference", reference.toString())
//            if (!alreadyMatches) {
//                reference.push().setValue(
//                    Match(currentUser, owner)
//                )
//            }
//        }
    }

    private fun onItemClicked() {

    }

    override fun getItemCount(): Int {
        return items.size
    }
}