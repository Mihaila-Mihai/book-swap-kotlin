package ro.example.bookswap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile_visualisation.*
import ro.example.bookswap.adapters.BooksAdapter
import ro.example.bookswap.adapters.PersonalBooksAdapter
import ro.example.bookswap.decoration.TopSpacingItemDecoration
import ro.example.bookswap.models.Book
import ro.example.bookswap.models.User

class ProfileVisualisationActivity : AppCompatActivity() {

    private lateinit var user: User
    private val ref: DatabaseReference = Firebase.database.reference
    private val currentUser: String = Firebase.auth.currentUser?.uid!!
    private lateinit var bookAdapter: PersonalBooksAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_visualisation)

        toolbar_like.setNavigationOnClickListener { finish() }
        toolbar_like.title = "User profile"

        val userId = intent.getStringExtra("userId")

        fillUserUI(userId)
        fullUserBooksUI(userId)

    }

    private fun fullUserBooksUI(userId: String?) {
        val books: ArrayList<Book> = ArrayList()
        Firebase.database.reference.child("books").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                books.clear()
                for (el in snapshot.children) {
                    val book = el.getValue<Book>()
                    if (book?.owner == currentUser) {
                        books.add(book)
                    }
                }

                likes_view_recycler.apply {
                    layoutManager = LinearLayoutManager(this@ProfileVisualisationActivity)
                    val topSpacingDecoration = TopSpacingItemDecoration(30)
                    addItemDecoration(topSpacingDecoration)
                    bookAdapter = PersonalBooksAdapter(books, this@ProfileVisualisationActivity)
                    adapter = bookAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun fillUserUI(userId: String?) {
        Firebase.database.reference.child("users").child(userId!!).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                user = snapshot.getValue<User>()!!
                Picasso.get().load(user.imageUrl).fit().centerCrop().into(user_image_like_view)
                username_like_view.text = user.username
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }


}