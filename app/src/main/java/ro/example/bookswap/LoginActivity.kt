package ro.example.bookswap

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import ro.example.bookswap.enums.Activities
import ro.example.bookswap.models.User

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN: Int = 120
    private lateinit var database: DatabaseReference

//    private val startForResult =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            Toast.makeText(baseContext, "InStart ${result.resultCode}", Toast.LENGTH_SHORT).show()
//            if (result.resultCode == Activity.RESULT_OK) {
////            val intent = result.data
//                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
//                Toast.makeText(baseContext, "Bello!", Toast.LENGTH_SHORT).show()
//                try {
//                    // Google sign in was successful
//                    val account = task.getResult(ApiException::class.java)
//                    Log.d(TAG, "firebaseAuthWithGoogle:" + account?.id)
//                    firebaseAuthWithGoogle(account?.idToken!!)
//                } catch (e: ApiException) {
//                    // Google sign in failed
//                    Log.w(TAG, "Google sign in failed.", e)
//                }
//            }
//        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth
        database = Firebase.database.reference

        findViewById<TextView>(R.id.register_text).setOnClickListener {
            startActivityCustom(Activities.REGISTER)
        }
        findViewById<Button>(R.id.login_button).setOnClickListener {
            val email: String = findViewById<EditText>(R.id.login_email).text.toString()
            val password: String = findViewById<EditText>(R.id.login_password).text.toString()
            signInWithEmail(email, password)
        }

        findViewById<SignInButton>(R.id.google_login_button).setOnClickListener {
            signInWithGoogle()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
//        startForResult.launch(signInIntent)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Toast.makeText(baseContext, "$resultCode ${Activity.RESULT_OK}", Toast.LENGTH_SHORT).show()
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google sign in was successful
                val account = task.getResult(ApiException::class.java)
                Log.d(TAG, "firebaseAuthWithGoogle:" + account?.id)
                firebaseAuthWithGoogle(account?.idToken!!)
            } catch (e: ApiException) {
                // Google sign in failed
                Log.w(TAG, "Google sign in failed.", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    Log.d(TAG, "signInWithCredential:success")
                    val isNew = task.result?.additionalUserInfo?.isNewUser
                    if (isNew == true) {
                        writeNewUser(Firebase.auth.currentUser?.uid!!)
                        Log.d("UserID", Firebase.auth.currentUser?.uid.toString())
                        startActivityCustom(Activities.TUTORIAL)
                    } else {
                        startActivityCustom(Activities.MAIN)
                    }
                } else {
                    // If sign in fails, display a message to the user
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun writeNewUser(userId: String) {
        val currentUser = Firebase.auth.currentUser!!
        val user = currentUser.displayName?.let { currentUser.email?.let { it1 ->
            User(it, "",
                it1, "", "Google", currentUser.photoUrl.toString())
        } }
        database.child("users").child(userId).setValue(user).addOnCompleteListener(
            OnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "LoginActivityDatabaseWrite:success")
            } else {
                Log.e(TAG, "LoginActivityDatabaseWrite:fail", task.exception)
            }
            })
    }

    private fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    Log.d(TAG, "signInWithEmail:success")
                    startActivityCustom(Activities.MAIN)
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }



    override fun onStart() {
        super.onStart()

        // Check if user is signed in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            startActivityCustom(Activities.MAIN)
        }
    }



    private fun startActivityCustom(activity: Activities) {
        val intent = when (activity) {
            Activities.MAIN -> Intent(this, MainActivity::class.java)
            Activities.REGISTER -> Intent(this, RegisterActivity::class.java)
            Activities.TUTORIAL -> Intent(this, TutorialActivity::class.java)
        }
        startActivity(intent)
        if (activity == Activities.MAIN) {
            finish()
        }
    }
}