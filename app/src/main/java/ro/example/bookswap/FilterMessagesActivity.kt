package ro.example.bookswap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import ro.example.bookswap.models.Message
import ro.example.bookswap.models.User

class FilterMessagesActivity: AppCompatActivity() {
    private val reference = Firebase.database.reference
    private val currentUser = Firebase.auth.currentUser?.uid
    private lateinit var userId: String
    private lateinit var user: User
    var messages: ArrayList<Message> = ArrayList()

    val TAG = "MessageActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}