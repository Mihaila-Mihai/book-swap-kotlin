package ro.example.bookswap

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import ro.example.bookswap.enums.Activities

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 120

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = Firebase.auth


        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)


        findViewById<SignInButton>(R.id.sign_in_with_google_button).setOnClickListener(View.OnClickListener { signIn() })
        findViewById<Button>(R.id.sign_in_with_email_and_password_button).setOnClickListener(
            View.OnClickListener { emailAndPasswordSignIn() })
        findViewById<Button>(R.id.create_account_button).setOnClickListener(View.OnClickListener { register() })


    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign-In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    val isNew: Boolean? = task.result?.additionalUserInfo?.isNewUser
                    if (isNew == true) {
                        startActivityCustom(Activities.TUTORIAL)
                    } else {
                        startActivityCustom(Activities.MAIN)
                    }
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                }
            }
    }


    override fun onStart() {
        super.onStart()

        // Check if the user is signed-in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            startActivityCustom(Activities.MAIN)
        }
    }

    private fun startActivityCustom(activity: Activities) {
        var intent: Intent? = when (activity) {
            Activities.MAIN -> Intent(this, MainActivity::class.java)
            Activities.TUTORIAL -> Intent(this, TutorialActivity::class.java)

            else -> null
        }
        startActivity(intent)
        finish()
    }

    private fun emailAndPasswordSignIn() {
        val email: String = findViewById<EditText>(R.id.sign_in_email).text.toString()
        val password: String = findViewById<EditText>(R.id.sign_in_password).text.toString()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in successful
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    startActivityCustom(Activities.MAIN)
                } else {
                    // Sign in fails
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun register() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}