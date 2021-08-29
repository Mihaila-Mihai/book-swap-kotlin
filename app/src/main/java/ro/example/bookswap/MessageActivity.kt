package ro.example.bookswap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ro.example.bookswap.adapters.MessageAdapter
import ro.example.bookswap.enums.Status
import ro.example.bookswap.models.*
import java.lang.Exception

const val TOPIC = "/topics/myTopic"

class MessageActivity : AppCompatActivity() {

    private val reference = Firebase.database.reference
    private val currentUser = Firebase.auth.currentUser?.uid
    private lateinit var userId: String

    val TAG = "MessageActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)

        userId = intent.getStringExtra("user")!!

        toolbar.setNavigationOnClickListener {
            finish()
        }

        send_button.setOnClickListener { sendMessage() }

        profile_image_message.setOnClickListener {
            val intent = Intent(this, ProfileVisualisationActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }
        username.setOnClickListener {
            val intent = Intent(this, ProfileVisualisationActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.unmatch -> {
//                    Toast.makeText(this, "Unmatch :(", Toast.LENGTH_SHORT).show()
                    // TODO - implement unmatch functionality

                    Firebase.database.reference.child("matches").get().addOnSuccessListener {
                        for (el in it.children) {
                                val match = el.getValue<Match>()
                                if ((match?.userId1 == currentUser && match?.userId2 == userId) || (match?.userId1 == userId && match.userId2 == currentUser)) {
                                    Firebase.database.reference.child("matches").child(el.key!!).removeValue().addOnSuccessListener {
                                        Firebase.database.reference.child("likes").child(userId).get().addOnSuccessListener { snapshotLikes ->
                                                for (elem in snapshotLikes.children) {
                                                    val like = elem.getValue<Like>()
                                                    if (like?.userId == currentUser) {
                                                        Firebase.database.reference.child("likes").child(userId).child(elem.key!!).removeValue()
                                                    }
                                                }
                                        }

                                        Firebase.database.reference.child("likes").child(currentUser!!).get().addOnSuccessListener { snapshotLike ->
                                                for (elem in snapshotLike.children) {
                                                    val like = elem.getValue<Like>()
                                                    if (like?.userId == userId) {
                                                        Firebase.database.reference.child("likes").child(currentUser).child(elem.key!!).removeValue()
                                                    }
                                                }
                                        }

                                        Firebase.database.reference.child("swaps").get().addOnSuccessListener { snapshotSwap ->
                                            for (elem in snapshotSwap.children) {
                                                val swap = elem.getValue<Swap>()
                                                if (swap?.sender == currentUser && swap.receiver == userId || swap?.sender == userId && swap.receiver == currentUser) {
                                                    Firebase.database.reference.child("swaps").child(swap.id).child("status").setValue(Status.DECLINED)
                                                }
                                            }
                                        }

                                        finish()
                                    }
                                }
                            }
                    }

                    true
                }
                R.id.swap -> {
//                    Toast.makeText(this, "swap", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, SwapActivity::class.java)
                    intent.putExtra("userId", userId)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }


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
                val title = "New message"
                val text = text_send.text.toString()
                Firebase.database.reference.child("users").child(userId).child("token").get().addOnSuccessListener {
                    if (title.isNotEmpty() && text.isNotEmpty()) {
                        PushNotification(
                            NotificationData("${currentUser}+${title}", text),
                            it.value.toString()
                        ).also { value ->
                            sendNotification(value)
                        }
                    }
                }


                text_send.setText("")
            }.addOnFailureListener {
                Toast.makeText(this, "There was a problem", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Text can't be empty", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful) {
                Log.d(TAG, "Response:")
            } else {
                Log.e(TAG + "eee", response.errorBody().toString())
            }
        } catch (e: Exception) {
            Log.e(TAG, e.toString(), e.cause)
        }
    }
}