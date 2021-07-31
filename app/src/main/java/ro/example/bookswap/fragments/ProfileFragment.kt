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
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import ro.example.bookswap.AddBookActivity
import ro.example.bookswap.ProfileActivity
import ro.example.bookswap.R

class ProfileFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var profileImage: CircleImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

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
        profileImage = view.findViewById(R.id.profile_image)

        setProfilePicture()

        view.findViewById<AppCompatImageButton>(R.id.settings_button).setOnClickListener(View.OnClickListener {
            val intent = Intent(context, ProfileActivity::class.java)

            startActivity(intent,
                ActivityOptions.makeSceneTransitionAnimation(activity).toBundle())

        })
        view.findViewById<AppCompatImageButton>(R.id.add_book_button).setOnClickListener {
            val intent = Intent(context, AddBookActivity::class.java)

            startActivity(intent)
        }



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