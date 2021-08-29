package ro.example.bookswap.fragments

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.add_book_methods_dialog.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import ro.example.bookswap.*
import ro.example.bookswap.adapters.PersonalBooksAdapter
import ro.example.bookswap.decoration.TopSpacingItemDecoration
import ro.example.bookswap.models.Book
import ro.example.bookswap.models.User

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

        view.findViewById<AppCompatImageButton>(R.id.settings_button).setOnClickListener {
            val intent = Intent(context, ProfileActivity::class.java)

            startActivity(
                intent,
                ActivityOptions.makeSceneTransitionAnimation(activity).toBundle()
            )

        }
        view.findViewById<AppCompatImageButton>(R.id.add_book_button).setOnClickListener {
//            val intent = Intent(context, AddBookActivity::class.java)

            val dialog = MaterialAlertDialogBuilder(requireContext())
            dialog.setView(R.layout.add_book_methods_dialog)
            val dialogCreated = dialog.create()
            dialogCreated.show()


            dialogCreated.manual_input.setOnClickListener {
                dialogCreated.dismiss()
                val intent = Intent(context, BookVisualisationActivity::class.java)
                intent.putExtra("personal", "fromProfile")
                startActivity(intent)
            }

            dialogCreated.title_search.setOnClickListener {
                dialogCreated.manual_input.visibility = View.GONE
                dialogCreated.manual_text.visibility = View.GONE
                dialogCreated.title_search.visibility = View.GONE
                dialogCreated.search_text.visibility = View.GONE
                dialogCreated.photo.visibility = View.GONE
                dialogCreated.photo_text.visibility = View.GONE

                dialogCreated.outlinedTextField.visibility = View.VISIBLE
                dialogCreated.search_button.visibility = View.VISIBLE
            }

            dialogCreated.search_button.setOnClickListener {
                dialogCreated.dismiss()
                if (dialogCreated.input_content.text!!.isNotEmpty()) {
                    val intent = Intent(context, AddBookActivity::class.java)
                    intent.putExtra("searchString", dialogCreated.input_content.text.toString())
                    intent.putExtra("from", "title")
                    startActivity(intent)
                }
            }

            dialogCreated.photo.setOnClickListener {
                dialogCreated.dismiss()
                val intent = Intent(context, CameraActivity::class.java)
                intent.putExtra("activity", "profileFragment")
                startActivity(intent)
            }

        }


        val booksReference = database.child("books")
        val currentUser = Firebase.auth.currentUser?.uid
        val books: ArrayList<Book> = ArrayList()

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

            }

        })


        view.history_button.setOnClickListener {
            val intent = Intent(context, HistoryActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun setProfilePicture() {
        val uid = Firebase.auth.currentUser?.uid!!
        database.child("users").child(uid).get().addOnSuccessListener {
            val user : User? = it.getValue<User>()
            if (user?.imageUrl == "default") {
                profileImage.setImageResource(R.drawable.ic_user)
            } else {
                try {
                    Glide.with(this).load(user?.imageUrl).into(profileImage)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            username_swipe_view.text = user?.username
        }.addOnFailureListener {
            Toast.makeText(context, "Profile picture load failed", Toast.LENGTH_SHORT).show()
            Log.e("profileActivityPicture:", "failed", it.cause)
        }

    }

}