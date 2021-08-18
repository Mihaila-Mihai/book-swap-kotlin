package ro.example.bookswap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_add_book.*
import kotlinx.android.synthetic.main.activity_history.*
import ro.example.bookswap.adapters.HistoryAdapter
import ro.example.bookswap.adapters.PersonalBooksAdapter
import ro.example.bookswap.adapters.SwapsAdapter
import ro.example.bookswap.decoration.TopSpacingItemDecoration
import ro.example.bookswap.enums.Status
import ro.example.bookswap.models.Swap

class HistoryActivity : AppCompatActivity() {

    private lateinit var swapsAdapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        toolbar_history.setNavigationOnClickListener { finish() }
        toolbar_history.title = "History"

        val swaped: ArrayList<Swap> = ArrayList()

        Firebase.database.reference.child("swaps").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                swaped.clear()
                for (el in snapshot.children) {
                    val swap: Swap? = el.getValue<Swap>()
                    if (swap?.status != Status.IN_PROGRESS) {
                        swaped.add(swap!!)
                    }
                }

                recycler_view_history.apply {
                    layoutManager = LinearLayoutManager(context)
                    val topSpacingDecoration = TopSpacingItemDecoration(30)
                    addItemDecoration(topSpacingDecoration)
                    swapsAdapter = HistoryAdapter(swaped, context)
                    adapter = swapsAdapter
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


    }
}