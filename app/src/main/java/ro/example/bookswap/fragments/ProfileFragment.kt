package ro.example.bookswap.fragments

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import ro.example.bookswap.ProfileActivity
import ro.example.bookswap.R

class ProfileFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val profileImage = view.findViewById<CircleImageView>(R.id.profile_image)

        Glide.with(this).load(Firebase.auth.currentUser?.photoUrl).into(profileImage)

        view.findViewById<AppCompatImageButton>(R.id.settings_button).setOnClickListener(View.OnClickListener {
            val intent = Intent(context, ProfileActivity::class.java)

            startActivity(intent,
                ActivityOptions.makeSceneTransitionAnimation(activity).toBundle())

        })



        return view
    }

}