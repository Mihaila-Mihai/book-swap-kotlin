package ro.example.bookswap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.activity_profile_visualisation.*
import kotlinx.android.synthetic.main.activity_swap.*
import ro.example.bookswap.adapters.SwapBooksAdapter
import ro.example.bookswap.models.Like
import ro.example.bookswap.models.Swap
import ro.example.bookswap.models.User
import ro.example.bookswap.singletons.SwapItems

class SwapActivity : AppCompatActivity() {

    private lateinit var user: User
    private val ref = Firebase.database.reference
    private val currentUser = Firebase.auth.currentUser?.uid
    private lateinit var bookAdapter: SwapBooksAdapter


    override fun onStop() {
        super.onStop()
        SwapItems.myBook = null
        SwapItems.userBook = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swap)

        val userId = intent.getStringExtra("userId")

        toolbar_swap.setNavigationOnClickListener {
            finish()
        }
        toolbar_swap.title = "Swap"

        ref.child("users").child(userId!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                user = snapshot.getValue<User>()!!
                initialiseUI()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


        books_swap_button.setOnClickListener { requestSwap() }

    }

    private fun requestSwap() {
        if (SwapItems.userBook != null && SwapItems.myBook != null) {
//            Log.d("ia sa vedem", "${SwapItems.userBook?.id.toString()} \n ${SwapItems.myBook?.id.toString()}")
            val id = (System.currentTimeMillis() / 1000 + (1..100000).random()).toString()
            val swap = Swap(currentUser!!, user.id, SwapItems.myBook?.id!!, SwapItems.userBook?.id!!, id)

            ref.child("swaps").child(id).setValue(swap).addOnSuccessListener {
                Toast.makeText(this, "Swap request send!", Toast.LENGTH_SHORT).show()
                finish()
            }


        } else {
            Toast.makeText(this, "You have to select the books!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initialiseUI() {
        Picasso.get().load(user.imageUrl).into(user_image_swap)
        username_swap_view.text = String.format(
            resources.getString(R.string.swap_user_text),
            user.username
        )

        val myLikes: ArrayList<Like> = ArrayList()

        ref.child("likes").child(user.id).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                myLikes.clear()

                for (el in snapshot.children) {
                    val like = el.getValue<Like>()
                    if (like?.userId == currentUser) {
                        myLikes.add(like!!)
                    }
                }

                user_books.apply {
                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    val snapHelper = LinearSnapHelper()
                    try {
                        snapHelper.attachToRecyclerView(user_books)
                    } catch (e: Exception) {
                        Log.d("SwapActivity:", "snapHelper", e)
                    }
                    bookAdapter = SwapBooksAdapter(myLikes, this@SwapActivity, "user_books")
                    adapter = bookAdapter
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        val likes: ArrayList<Like> = ArrayList()

        ref.child("likes").child(currentUser!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                likes.clear()

                for (el in snapshot.children) {
                    val like = el.getValue<Like>()
                    if (like?.userId == user.id) {
                        likes.add(like)
                    }
                }

                my_books.apply {
                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    val snapHelper = LinearSnapHelper()
                    try {
                        snapHelper.attachToRecyclerView(my_books)
                    } catch (e: Exception) {
                        Log.e("SwapActivity:", "snapHelper", e)
                    }
                    bookAdapter = SwapBooksAdapter(likes, this@SwapActivity, "my_books")
                    adapter = bookAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        ref.child("users").child(currentUser).get().addOnSuccessListener {
            val loggedUser = it.getValue<User>()
            Picasso.get().load(loggedUser?.imageUrl).into(my_image)
        }


    }
}