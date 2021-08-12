package ro.example.bookswap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message.*
import ro.example.bookswap.adapters.MessageAdapter
import ro.example.bookswap.models.Message
import ro.example.bookswap.models.User

class MessageActivity : AppCompatActivity() {

    private val reference = Firebase.database.reference
    private val currentUser = Firebase.auth.currentUser?.uid
    private lateinit var userId: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        send_button.setOnClickListener { sendMessage() }

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.unmatch -> {
                    Toast.makeText(this, "Unmatch :(", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.swap -> {
                    Toast.makeText(this, "swap", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        userId = intent.getStringExtra("user")!!
        val userRef = reference.child("users").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue<User>()
                    username.text = user?.username
                    Picasso.get().load(user?.imageUrl).into(profile_image_message)
                    readMessages(user?.imageUrl!!)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

    }

    private fun readMessages(imageUrl: String) {
        val messages: ArrayList<Message> = ArrayList()

        val messageRef =
            reference.child("chats").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages.clear()
                    for (message in snapshot.children) {
                        val mess = message.getValue<Message>()
                        if ((mess?.sender == currentUser && mess?.receiver == userId) || (mess?.sender == userId && mess.receiver == currentUser)) {
                            messages.add(mess)
                        }
                        msg_recycler_view.apply {
                            val manager = LinearLayoutManager(this@MessageActivity)
                            manager.stackFromEnd = true
                            layoutManager = manager
                            val messageAdapter = MessageAdapter(messages, this@MessageActivity, imageUrl)
                            adapter = messageAdapter
                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun sendMessage() {
        if (text_send.text.isNotEmpty()) {
            val id = (System.currentTimeMillis() / 1000).toString()
            val messageRef = reference.child("chats").child(id)
            val message = Message(currentUser!!, userId, id, text_send.text.toString())
            messageRef.setValue(message).addOnSuccessListener {
                text_send.setText("")
            }.addOnFailureListener {
                Toast.makeText(this, "There was a problem", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "merge", Toast.LENGTH_SHORT).show()
        }
    }
}