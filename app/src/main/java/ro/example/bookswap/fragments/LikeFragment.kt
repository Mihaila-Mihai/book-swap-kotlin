package ro.example.bookswap.fragments

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_like.*
import ro.example.bookswap.R
import ro.example.bookswap.adapters.LikesAdapter
import ro.example.bookswap.decoration.TopSpacingItemDecoration
import ro.example.bookswap.models.Like


class LikeFragment : Fragment() {

    private lateinit var likeAdapter: LikesAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view: View? = inflater.inflate(R.layout.fragment_like, container, false)

        val reference =
            Firebase.database.reference.child("likes").child(Firebase.auth.currentUser?.uid!!)

        val likedBy: ArrayList<Like> = ArrayList()

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (el in snapshot.children) {
                    val like = el.getValue<Like>()
                    likedBy.add(like!!)
                }
                Log.d("size", likedBy.size.toString())

                try {
                    likes_recycler.apply {
                        layoutManager = LinearLayoutManager(view?.context)
                        val topSpacingDecoration = TopSpacingItemDecoration(30)
                        addItemDecoration(topSpacingDecoration)
                        likeAdapter = LikesAdapter(likedBy, view?.context!!)
                        adapter = likeAdapter
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Recycler", e)
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        return view
    }

}