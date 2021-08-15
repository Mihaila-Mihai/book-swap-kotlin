package ro.example.bookswap.fragments

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_notifications.*
import ro.example.bookswap.R
import ro.example.bookswap.adapters.LikesAdapter
import ro.example.bookswap.adapters.ViewPagerAdapter
import ro.example.bookswap.decoration.TopSpacingItemDecoration
import ro.example.bookswap.models.Like


val tabsArray = arrayOf(
    "Likes",
    "Swaps"
)

class NotificationsFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View? = inflater.inflate(R.layout.fragment_notifications, container, false)


        val tabLayout = view?.findViewById<TabLayout>(R.id.tab_layout)
        val viewPager2 = view?.findViewById<ViewPager2>(R.id.view_pager)!!

        val adapter2 = ViewPagerAdapter(parentFragmentManager, lifecycle)
        viewPager2.adapter = adapter2

        TabLayoutMediator(tabLayout!!, viewPager2) { tab, position ->
            tab.text = tabsArray[position]
        }.attach()



        return view
    }



}