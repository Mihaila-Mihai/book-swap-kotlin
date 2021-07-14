package ro.example.bookswap

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import ro.example.bookswap.fragments.ChangePasswordDialogFragment
import ro.example.bookswap.fragments.ExitDialogFragment

class ProfileActivity : AppCompatActivity(), ChangePasswordDialogFragment.NoticeDialogListener {

    lateinit var user: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        user = Firebase.auth.currentUser!!



        val isEmailVerified: Boolean = user.isEmailVerified
        val text: String = when(isEmailVerified) {
            true -> "Email verified."
            false -> "Email not verified. Click here to verify."
        }
        editVerifiedAccount(text)
        if (!isEmailVerified) {
            findViewById<TextView>(R.id.verified_account).setOnClickListener(View.OnClickListener {
                sendVerificationEmail()
            })
        }

        val profileImage = findViewById<CircleImageView>(R.id.profile_image)
        Glide.with(this).load(user.photoUrl).into(profileImage)
        findViewById<EditText>(R.id.username_edit).hint = user.displayName
        findViewById<EditText>(R.id.email_address).hint = user.email
        findViewById<AppCompatButton>(R.id.reset_password_button).setOnClickListener(View.OnClickListener { resetPassword() })
    }

    private fun resetPassword() {
        showDialog()
    }

    private fun showDialog() {
        val dialog = ChangePasswordDialogFragment()
        dialog.show(supportFragmentManager, "ChangePasswordDialog")
    }

    private fun editVerifiedAccount(text: String) {
        findViewById<TextView>(R.id.verified_account).text = text
    }

    private fun sendVerificationEmail() {
        user.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(ContentValues.TAG, "Email sent.")
                }
            }
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        val oldPassword = dialog.dialog?.findViewById<EditText>(R.id.old_password)!!
        val newPassword = dialog.dialog?.findViewById<EditText>(R.id.new_password)!!

        dialog.dismiss()
        Toast.makeText(this, "Password successfully changed ${oldPassword.text.toString()} ${newPassword.text.toString()}", Toast.LENGTH_SHORT).show()
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        dialog.dismiss()
    }
}