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
import kotlinx.android.synthetic.main.fragment_chats.*
import ro.example.bookswap.R
import ro.example.bookswap.adapters.ChatsAdapter
import ro.example.bookswap.decoration.TopSpacingItemDecoration
import ro.example.bookswap.models.Match


class ChatsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chats, container, false)

        val currentUser = Firebase.auth.currentUser?.uid
        val reference = Firebase.database.reference.child("matches")

        val matches: ArrayList<String> = ArrayList()

        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (el in snapshot.children) {
                    val match = el.getValue<Match>()
                    if (match?.userId1 == currentUser) {
                        matches.add(match?.userId2!!)
                    } else {
                        if (match?.userId2 == currentUser) {
                            matches.add(match?.userId1!!)
                        }
                    }
                }

                try {
                    chats_recycler.apply {
                        layoutManager = LinearLayoutManager(view?.context)
                        val topSpacingDecoration = TopSpacingItemDecoration(30)
                        addItemDecoration(topSpacingDecoration)
                        val chatsAdapter = ChatsAdapter(matches, view?.context!!)
                        adapter = chatsAdapter
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "chatsFragment", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


        return view
    }

}