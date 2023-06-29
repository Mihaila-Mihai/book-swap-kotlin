package ro.example.bookswap

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_book_visualisation.*
import kotlinx.android.synthetic.main.activity_profile_visualisation.*
import ro.example.bookswap.adapters.PersonalBooksAdapter
import ro.example.bookswap.decoration.TopSpacingItemDecoration
import ro.example.bookswap.models.Book
import ro.example.bookswap.models.User

class ProfileVisualisationActivity : AppCompatActivity() {

    private lateinit var user: User
    private lateinit var bookAdapter: PersonalBooksAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_visualisation)

        toolbar_like.setNavigationOnClickListener { finish() }
        toolbar_like.title = "User profile"

        val userId = intent.getStringExtra("userId")

        fillUserUI(userId)

        toolbar_like.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId) {
                R.id.maps -> {
                    val sendIntent: Intent = Intent(this, MapsActivity::class.java).apply {
                        putExtra("LONGITUDE", user.location.longitude)
                        putExtra("LATITUDE", user.location.latitude)
                        type = "text/plain"
                    }
                    startActivity(sendIntent)

                    true
                }
                else -> false
            }
        }

    }

    private fun fillUserBooksUI() {
        val books: ArrayList<Book> = ArrayList()
        Firebase.database.reference.child("books").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                books.clear()
                for (el in snapshot.children) {
                    val book = el.getValue<Book>()
                    if (book?.owner == user.id) {
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
                fillUserBooksUI()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }


}