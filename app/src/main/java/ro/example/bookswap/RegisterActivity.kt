package ro.example.bookswap

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import at.favre.lib.crypto.bcrypt.BCrypt
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import ro.example.bookswap.enums.Activities
import ro.example.bookswap.models.User

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var database: DatabaseReference
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var username: EditText
    private lateinit var userId: String
    private lateinit var emailString: String
    private lateinit var passwordString: String
    private lateinit var usernameString: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth
        database = Firebase.database.reference
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        username = findViewById(R.id.username)

        findViewById<Button>(R.id.create_account).setOnClickListener {
            emailString = email.text.toString()
            passwordString = password.text.toString()
            createAccount(emailString, passwordString)
        }
    }

    private fun createAccount(emailString: String, passwordString: String) {
        auth.createUserWithEmailAndPassword(emailString, passwordString)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success update username and UI
                    Log.d(TAG, "createUserWithEmail: success")
                    user = Firebase.auth.currentUser!!
                    userId = Firebase.auth.currentUser?.uid!!
                    updateUsername()
                    val isNew: Boolean = task.result?.additionalUserInfo?.isNewUser ?: true
                    if (isNew) {
                        startActivityCustom(Activities.TUTORIAL)
                    } else {
                        startActivityCustom(Activities.MAIN)
                    }
                } else {
                    // Sign in fails
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateUsername() {
        usernameString = username.text.toString()
        val profileUpdates = userProfileChangeRequest {
            displayName = usernameString
        }

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User profile updated to ${user.displayName}.")
                    sendVerificationEmail()
                }
            }
    }

    private fun sendVerificationEmail() {
        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Email sent.")
                    val encryptedPassword =
                        BCrypt.withDefaults().hashToString(12, passwordString.toCharArray())
                    val user =
                        User(usernameString, encryptedPassword, emailString, "", "EmailAndPassword")
                    writeNewUser(user)
                }
            }
    }

    private fun writeNewUser(user: User) {
        database.child("users").child(userId).setValue(user)
    }


    private fun startActivityCustom(activity: Activities) {
        val intent = when (activity) {
            Activities.MAIN -> Intent(this, MainActivity::class.java)
            Activities.TUTORIAL -> Intent(this, TutorialActivity::class.java)
            else -> null
        }
        startActivity(intent)
        if (activity == Activities.MAIN) {
            finish()
        }
    }

}