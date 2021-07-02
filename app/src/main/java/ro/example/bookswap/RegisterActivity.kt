package ro.example.bookswap

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth

        findViewById<Button>(R.id.create_account).setOnClickListener {
            val email: String = findViewById<EditText>(R.id.email).text.toString()
            val password: String = findViewById<EditText>(R.id.password).text.toString()
            createAccount(email, password)
        }
    }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success update username and UI
                    Log.d(TAG, "createUserWithEmail: success")
                    user = Firebase.auth.currentUser!!
                    updateUsername()
                } else {
                    // Sign in fails
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateUsername() {
        val username: String = findViewById<EditText>(R.id.username).text.toString()

        val profileUpdates = userProfileChangeRequest {
            displayName = username
        }

        user.updateProfile(profileUpdates)
            .addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User profile updated to ${user.displayName}.")
                    sendVerificationEmail()
                    startMainActivity()
                }
            }
    }

    private fun sendVerificationEmail() {
        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Email sent.")
                }
            }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}