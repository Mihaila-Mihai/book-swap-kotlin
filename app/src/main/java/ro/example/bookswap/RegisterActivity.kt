package ro.example.bookswap

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth
        val createAccountButton: Button = findViewById(R.id.register_button)
        createAccountButton.setOnClickListener(View.OnClickListener { createAccount() })
    }

    private fun createAccount() {
        val email: String = findViewById<EditText>(R.id.register_email).text.toString()
        val password: String = findViewById<EditText>(R.id.register_password).text.toString()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    startTutorialActivity()
                    emailVerification()
                } else {
                    // Sign in fails
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun emailVerification() {
        val user = Firebase.auth.currentUser

        user!!.sendEmailVerification()
            .addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    Log.d(TAG, "Email sent.")
                }
            }
    }

    private fun startTutorialActivity() {
        val intent = Intent(this, TutorialActivity::class.java)
        startActivity(intent)
        finish()
    }
}