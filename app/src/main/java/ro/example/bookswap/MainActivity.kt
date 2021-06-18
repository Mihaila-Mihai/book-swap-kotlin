package ro.example.bookswap

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.navigation.NavigationBarView

const val EXTRA_MESSAGE = "ro.example.bookswap.MESSAGE"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toast: Toast = Toast.makeText(applicationContext, "text", Toast.LENGTH_SHORT)

        val navigationBarView: NavigationBarView = findViewById(R.id.bottom_nav)



        navigationBarView.setOnItemSelectedListener { item ->

            when (item.itemId) {
                R.id.page_1 -> {
                    toast.setText("Page 1")
                    toast.show()
                    true
                }
                R.id.page_2 -> {
                    toast.setText("Page 2")
                    toast.show()
                    true
                }
                R.id.page_3 -> {
                    toast.setText("Page 3")
                    toast.show()
                    true
                }
                R.id.page_4 -> {
                    toast.setText("Page 4")
                    toast.show()
                    true
                }
                else -> false
            }
        }

    }



}