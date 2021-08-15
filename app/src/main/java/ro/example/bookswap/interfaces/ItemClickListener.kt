package ro.example.bookswap.interfaces

import android.view.View

interface ItemClickListener {
    fun onClick(view: View, position: Int)
}