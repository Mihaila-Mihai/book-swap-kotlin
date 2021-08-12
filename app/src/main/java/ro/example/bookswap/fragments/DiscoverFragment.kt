package ro.example.bookswap.fragments

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.yuyakaido.android.cardstackview.*
import ro.example.bookswap.R
import ro.example.bookswap.adapters.CardStackAdapter
import ro.example.bookswap.models.Book

class DiscoverFragment : Fragment() {

    private lateinit var database: DatabaseReference

    private lateinit var manager: CardStackLayoutManager
    private lateinit var adapter: CardStackAdapter

    private var size: Int = 0
    private var position: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_discover, container, false)

        val books: ArrayList<Book> = ArrayList()

        val swipeView: CardStackView = view.findViewById(R.id.card_stack)

        manager = CardStackLayoutManager(context, object: CardStackListener {
            override fun onCardDragging(direction: Direction?, ratio: Float) {
                Log.d(TAG, "onCardDragging: d=" + direction?.name + "ratio=" + ratio)
            }

            override fun onCardSwiped(direction: Direction?) {
                Log.d(TAG, "onCardSwiped: p=")
                if (direction == Direction.Right) {
                    adapter.onSwipe(position)
                    position++
                }
                if (direction == Direction.Left) {
                    Toast.makeText(view.context, "Left", Toast.LENGTH_SHORT).show()
                    position++
                }
                if (direction == Direction.Top) {
                    Toast.makeText(view.context, "Top", Toast.LENGTH_SHORT).show()
                }
                if (direction == Direction.Bottom) {
                    Toast.makeText(view.context, "Bottom", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCardRewound() {
//                Toast.makeText(view?.context, "Card Rewound", Toast.LENGTH_SHORT).show()
            }

            override fun onCardCanceled() {
//                Toast.makeText(view?.context, "Card Canceled", Toast.LENGTH_SHORT).show()
            }

            override fun onCardAppeared(view: View?, position: Int) {
//                Toast.makeText(view?.context, "Card Appeared", Toast.LENGTH_SHORT).show()
            }

            override fun onCardDisappeared(view: View?, position: Int) {
//                Toast.makeText(view?.context, "Card Disappeared", Toast.LENGTH_SHORT).show()
            }

        })



        database = Firebase.database.reference
        val currentUser = Firebase.auth.currentUser?.uid!!
        database.child("books").get().addOnSuccessListener {
            for (bookElement in it.children) {
                Log.d("firebase", "Got value ${bookElement.value}")
                val book = bookElement.getValue<Book>()
                if (book?.owner != currentUser) {
                    books.add(book!!)
                }
            }

            size = books.size
            Log.d("Books", books.toString())

            manager.setStackFrom(StackFrom.None)
            manager.setVisibleCount(3)
            manager.setTranslationInterval(8.0F)
            manager.setScaleInterval(0.95F)
            manager.setSwipeThreshold(0.3F)
            manager.setMaxDegree(40.0F)
            manager.setDirections(Direction.HORIZONTAL)
            manager.setCanScrollHorizontal(true)
            manager.setSwipeableMethod(SwipeableMethod.Manual)
            manager.setOverlayInterpolator(LinearInterpolator())

            adapter = CardStackAdapter(books, view.context)
            swipeView.layoutManager = manager
            swipeView.adapter = adapter
            swipeView.itemAnimator = DefaultItemAnimator()

        }




        return view
    }

}