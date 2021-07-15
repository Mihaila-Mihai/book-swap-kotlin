package ro.example.bookswap

import android.R.attr.password
import android.content.ContentValues.TAG
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import at.favre.lib.crypto.bcrypt.BCrypt
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import org.w3c.dom.Text
import ro.example.bookswap.fragments.ChangePasswordDialogFragment
import ro.example.bookswap.models.User


class ProfileActivity : AppCompatActivity(), ChangePasswordDialogFragment.NoticeDialogListener {

    lateinit var user: FirebaseUser
    private lateinit var database: DatabaseReference
    private lateinit var profileImage: CircleImageView
    private lateinit var username: EditText
    private lateinit var emailAddress: EditText
    private lateinit var userDescription: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileImage = findViewById(R.id.profile_image)
        username = findViewById(R.id.username_edit)
        emailAddress = findViewById(R.id.email_address)
        userDescription = findViewById(R.id.user_description)
        user = Firebase.auth.currentUser!!
        database = Firebase.database.reference
        getUserInfo(Firebase.auth.currentUser?.uid!!)

        val isEmailVerified: Boolean = user.isEmailVerified
        val text: String = when (isEmailVerified) {
            true -> "Email verified."
            false -> "Email not verified. Click here to verify."
        }
        editVerifiedAccount(text)
        if (!isEmailVerified) {
            findViewById<TextView>(R.id.verified_account).setOnClickListener(View.OnClickListener {
                sendVerificationEmail()
            })
        }

        Glide.with(this).load(user.photoUrl).into(profileImage)



        findViewById<AppCompatButton>(R.id.change_password_button).setOnClickListener(View.OnClickListener { changePassword() })
    }

    private fun getUserInfo(uid: String) {
        val postReference = database.child("users").child(uid)
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userInfo = snapshot.getValue<User>()
                username.hint = userInfo?.username
                emailAddress.hint = userInfo?.email
                userDescription.setText(userInfo?.description)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", error.toException())
            }
        }
        postReference.addValueEventListener(postListener)
    }

    private fun changePassword() {
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
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Email sent.")
                }
            }
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        val reference = Firebase.database.reference
        val errorText = findViewById<TextView>(R.id.error_message)
        val oldPassword = dialog.dialog?.findViewById<EditText>(R.id.old_password)!!
        val newPassword = dialog.dialog?.findViewById<EditText>(R.id.new_password)!!

        if ((oldPassword.text.toString() != newPassword.text.toString()) and (newPassword.text.length > 7)) {
            reference.child("users").child(user.uid).child("password").get().addOnSuccessListener {
                Log.d("firebase:", "got value ${it.value}")
                val result: BCrypt.Result = BCrypt.verifyer()
                    .verify(oldPassword.text.toString().toCharArray(), it.value.toString())
                if (result.verified) {
                    updatePassword(newPassword.text.toString())
                }

            }
            dialog.dismiss()
        } else if (oldPassword.text.toString() == newPassword.text.toString()) {
            setErrorTextAndCounter(errorText, R.string.same_password)
        } else if (newPassword.text.length <= 7) {
            setErrorTextAndCounter(errorText, R.string.password_length)
        }




//        val result: BCrypt.Result = BCrypt.verifyer().verify(oldPassword.text.toString().toCharArray(), hashPassword.toString())
//        if (result.verified) {
//            Toast.makeText(this, "Password successfully changed ${oldPassword.text.toString()} ${newPassword.text.toString()}", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(this, hashPassword.toString(), Toast.LENGTH_SHORT).show()
//        }


//        Toast.makeText(this, "Password successfully changed ${oldPassword.text.toString()} ${newPassword.text.toString()}", Toast.LENGTH_SHORT).show()
    }

    private fun setErrorTextAndCounter(errorText: TextView?, text: Int) {
        errorText?.visibility = View.VISIBLE
        errorText?.text = getString(text)
        object: CountDownTimer(30000, 1000){
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                errorText?.visibility = View.GONE
            }
        }
    }


    private fun updatePassword(newPassword: String) {
        user.updatePassword(newPassword).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("firebaseAuth: ", "user password updated")
                val newPasswordHash =
                    BCrypt.withDefaults().hashToString(12, newPassword.toCharArray())
                database.child("users").child(user.uid).child("password").setValue(newPasswordHash)
                    .addOnSuccessListener {
                        Log.d("firebaseDatabase:", "user password updated")
                        Toast.makeText(this, "Password successfully updated", Toast.LENGTH_SHORT)
                            .show()
                    }
            } else {
                Log.d("firebaseAuth: ", "password change failed")
                Toast.makeText(this, "Password change failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        dialog.dismiss()
    }
}