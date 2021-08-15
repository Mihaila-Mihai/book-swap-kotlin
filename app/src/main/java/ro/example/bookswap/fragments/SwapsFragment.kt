package ro.example.bookswap.fragments

import android.os.Bundle
import android.renderscript.Sampler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_swaps.*
import ro.example.bookswap.R
import ro.example.bookswap.adapters.LikesAdapter
import ro.example.bookswap.adapters.SwapsAdapter
import ro.example.bookswap.decoration.TopSpacingItemDecoration
import ro.example.bookswap.models.Swap


class SwapsFragment : Fragment() {

    val ref = Firebase.database.reference
    val currentUser = Firebase.auth.currentUser?.uid
    private lateinit var swapsAdapter: SwapsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View? = inflater.inflate(R.layout.fragment_swaps, container, false)

        val swaps: ArrayList<Swap> = ArrayList()

        ref.child("swaps").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                swaps.clear()
                for (el in snapshot.children) {
                    val swap = el.getValue<Swap>()
                    if (swap?.sender == currentUser || swap?.receiver == currentUser) {
                        swaps.add(swap!!)
                    }
                }

                swaps_recycler.apply {
                    layoutManager = GridLayoutManager(context, 2)
                    val topSpacingDecoration = TopSpacingItemDecoration(30)
                    addItemDecoration(topSpacingDecoration)
                    swapsAdapter = SwapsAdapter(swaps, view?.context!!)
                    adapter = swapsAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


        return view
    }

}