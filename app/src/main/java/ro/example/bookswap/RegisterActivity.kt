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
import at.favre.lib.crypto.bcrypt.BCrypt
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_register.*
import ro.example.bookswap.models.LocationModel
import ro.example.bookswap.models.User

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        toolbar_register.setNavigationOnClickListener { finish() }
        toolbar_register.title = "Register"

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
                    val username = register_username.text.toString()
//                    val passwordText = register_password.text.toString()
                    val hashPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())
                    val emailText = register_email.text.toString()
                    val newUser = User(
                        username = username,
                        password = hashPassword,
                        email = emailText,
                        description = "",
                        provider = user?.providerId.toString(),
                        imageUrl = "default",
                        id = user?.uid!!,
                        location = LocationModel()
                    )

                    Firebase.database.reference.child("users").child(user.uid).setValue(newUser)
                    startMainActivity()
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

    private fun startMainActivity() {
        finish()
    }
}