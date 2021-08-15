package ro.example.bookswap.fragments

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import ro.example.bookswap.CameraActivity
import ro.example.bookswap.ProfileActivity
import ro.example.bookswap.R
import ro.example.bookswap.adapters.PersonalBooksAdapter
import ro.example.bookswap.decoration.TopSpacingItemDecoration
import ro.example.bookswap.models.Book

class ProfileFragment : Fragment() {

//    private val cropActivityResultContract = object: ActivityResultContract<Any?, Uri?>

    private lateinit var database: DatabaseReference
    private lateinit var profileImage: CircleImageView
    private lateinit var bookAdapter: PersonalBooksAdapter


    override fun onResume() {
        super.onResume()
        setProfilePicture()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        database = Firebase.database.reference
        profileImage = view.findViewById(R.id.profile_image_message)

        setProfilePicture()

        view.findViewById<AppCompatImageButton>(R.id.settings_button).setOnClickListener(View.OnClickListener {
            val intent = Intent(context, ProfileActivity::class.java)

            startActivity(intent,
                ActivityOptions.makeSceneTransitionAnimation(activity).toBundle())

        })
        view.findViewById<AppCompatImageButton>(R.id.add_book_button).setOnClickListener {
//            val intent = Intent(context, AddBookActivity::class.java)
            val intent = Intent(context, CameraActivity::class.java)
            intent.putExtra("activity", "profileFragment")
            startActivity(intent)
        }


        val booksReference = database.child("books")
        val currentUser = Firebase.auth.currentUser?.uid
        var books: ArrayList<Book> = ArrayList()

        booksReference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                books.clear()

                for (bookElement in snapshot.children) {
                    val book = bookElement.getValue<Book>()

                    if (book?.owner == currentUser) {
                        books.add(book!!)
                    }
                }

                view.book_list.apply {
                    layoutManager = LinearLayoutManager(view.context)
                    val topSpacingDecoration = TopSpacingItemDecoration(30)
                    addItemDecoration(topSpacingDecoration)
//                    val snapHelper = LinearSnapHelper()
//                    snapHelper.attachToRecyclerView(view.book_list)
                    bookAdapter = PersonalBooksAdapter(books, view.context)
                    adapter = bookAdapter
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


        return view
    }

    private fun setProfilePicture() {
        val uid = Firebase.auth.currentUser?.uid!!
        database.child("users").child(uid).child("imageUrl").get().addOnSuccessListener {
            if (it.value == "default") {
                profileImage.setImageResource(R.drawable.ic_user)
            } else {
                try {
                    Glide.with(this).load(it.value).into(profileImage)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Profile picture load failed", Toast.LENGTH_SHORT).show()
            Log.e("profileActivityPicture:", "failed", it.cause)
        }

    }

}