package ro.example.bookswap

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.DialogFragment
import at.favre.lib.crypto.bcrypt.BCrypt
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_profile.*
import ro.example.bookswap.fragments.ChangePasswordDialogFragment
import ro.example.bookswap.models.User
import java.io.ByteArrayOutputStream


class ProfileActivity : AppCompatActivity(), ChangePasswordDialogFragment.NoticeDialogListener {

    private lateinit var user: FirebaseUser
    private lateinit var database: DatabaseReference
    private lateinit var profileImage: ImageView
    private lateinit var username: EditText
    private lateinit var emailAddress: EditText
    private lateinit var userDescription: EditText
    private lateinit var googleSignInClient: GoogleSignInClient
    private val storage = Firebase.storage

    override fun onResume() {
        super.onResume()
        setProfilePicture()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        toolbar_settings.setNavigationOnClickListener { finish() }
        toolbar_settings.title = "Settings"
        toolbar_settings.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId) {
                R.id.sign_out -> {
                    signOut()
                    true
                }
                else -> false
            }
        }

        profileImage = findViewById(R.id.profile_image_message)
        username = findViewById(R.id.username_edit)
        emailAddress = findViewById(R.id.email_address)
        userDescription = findViewById(R.id.user_description)
        user = Firebase.auth.currentUser!!
        database = Firebase.database.reference
        getUserInfo(Firebase.auth.currentUser?.uid!!)
        setProfilePicture()
        val isEmailVerified: Boolean = user.isEmailVerified
        val text: String = when (isEmailVerified) {
            true -> "Email verified."
            false -> "Email not verified. Click here to verify."
        }
        editVerifiedAccount(text)
        if (!isEmailVerified) {
            findViewById<TextView>(R.id.verified_account).setOnClickListener {
                sendVerificationEmail()
            }
        }


        findViewById<AppCompatButton>(R.id.change_password_button).setOnClickListener { changePassword() }
        findViewById<AppCompatImageButton>(R.id.username_apply).setOnClickListener { changeUsername() }
        findViewById<AppCompatImageButton>(R.id.about_you_apply).setOnClickListener { changeUserDescription() }
        findViewById<AppCompatImageButton>(R.id.email_address_button).setOnClickListener { changeEmailAddress() }
//        findViewById<Button>(R.id.sign_out_button).setOnClickListener { signOut() }
        findViewById<ImageView>(R.id.profile_image_message).setOnClickListener { changeProfileImage() }



    }


    private fun signOut() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Firebase.auth.signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
    }

    private fun changeEmailAddress() {
        val email = emailAddress.text.toString()
        hideKeyboard(this)
        when {
            email.isEmpty() -> {
                Toast.makeText(this, "Email can't be empty", Toast.LENGTH_SHORT).show()
            }
            android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                user.updateEmail(email).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d("profileActivityEmail:", user.isEmailVerified.toString() + user.email)
                        database.child("users").child(user.uid).child("email").setValue(email)
                            .addOnSuccessListener {
                                Log.d(TAG, "User email address updated")
                                Toast.makeText(this, "Email address updated", Toast.LENGTH_SHORT)
                                    .show()
                            }
                    } else {
                        Log.e(TAG, "User email address update failed", it.exception)
                        Toast.makeText(this, "Email address update failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            else -> {
                Toast.makeText(this, "Email address is not valid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun changeUserDescription() {
        val description = userDescription.text.toString()
        hideKeyboard(this)
        if (description.isEmpty()) {
            Toast.makeText(this, "Description can't be empty", Toast.LENGTH_SHORT).show()
        } else {
            database.child("users").child(user.uid).child("description").setValue(description)
                .addOnCompleteListener {
                    Toast.makeText(this, "Description updated", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Description update failed", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun changeUsername() {
        val name: String = username.text.toString()
        username.setText("")
        if (name.isEmpty()) {
            Toast.makeText(this, "Username can't be empty", Toast.LENGTH_SHORT).show()
        } else {
            val profileUpdates = userProfileChangeRequest {
                displayName = name
            }
            user.updateProfile(profileUpdates).addOnCompleteListener {
                if (it.isSuccessful) {
                    hideKeyboard(this)
                    database.child("users").child(user.uid).child("username").setValue(name)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Username successfully updated",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                }
            }
        }
    }

    private fun hideKeyboard(profileActivity: ProfileActivity) {
        val imm: InputMethodManager =
            profileActivity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = profileActivity.currentFocus
        if (view == null) {
            view = View(profileActivity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setProfilePicture() {
        val uid = Firebase.auth.currentUser?.uid!!
        database.child("users").child(uid).child("imageUrl").get().addOnSuccessListener {
            if (it.value == "default") {
                profileImage.setImageResource(R.drawable.ic_user)
            } else {
                try {
                    Picasso.get().load(it.value.toString()).fit().centerCrop().into(profileImage)
//                    Glide.with(this).load(it.value).into(profileImage)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Profile picture load failed", Toast.LENGTH_SHORT).show()
            Log.e("profileActivityPicture:", "failed", it.cause)
        }

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

        when {
            (oldPassword.text.toString() != newPassword.text.toString()) and (newPassword.text.length > 7) -> {
                reference.child("users").child(user.uid).child("password").get()
                    .addOnSuccessListener {
                        Log.d("firebase:", "got value ${it.value}")
                        val result: BCrypt.Result = BCrypt.verifyer()
                            .verify(oldPassword.text.toString().toCharArray(), it.value.toString())
                        if (result.verified) {
                            updatePassword(newPassword.text.toString())
                        }

                    }
                dialog.dismiss()
            }
            oldPassword.text.toString() == newPassword.text.toString() -> {
                setErrorTextAndCounter(errorText, R.string.same_password)
            }
            newPassword.text.length <= 7 -> {
                setErrorTextAndCounter(errorText, R.string.password_length)
            }
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
        object : CountDownTimer(30000, 1000) {
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

    private fun changeProfileImage() {
        val dialog = MaterialAlertDialogBuilder(this)
        dialog.setView(R.layout.image_change_method_dialog)
        val dialogCreated = dialog.create()
        dialogCreated.show()
        dialogCreated.findViewById<ImageButton>(R.id.camera_button)?.setOnClickListener {
            dialogCreated.dismiss()
            val intent = Intent(this, CameraActivity::class.java)
//            val intent = Intent(this, PhotoApparatActivity::class.java)
            startActivity(intent)
        }


        dialogCreated.findViewById<ImageButton>(R.id.media)?.setOnClickListener {
            dialogCreated.dismiss()

            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, 203)


        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 203) {
            if (resultCode == RESULT_OK) {
                val imageUri: Uri? = data?.data
                val imageStream = contentResolver.openInputStream(imageUri!!)
                val selectedImage = BitmapFactory.decodeStream(imageStream)
                uploadImage(selectedImage)
            }
        }
    }

    private fun uploadImage(selectedImage: Bitmap?) {
        val storageRef = storage.reference
        val imageRef: StorageReference? = Firebase.auth.currentUser?.let {
            storageRef.child("profile-images").child(it.uid)
                .child((System.currentTimeMillis() / 1000).toString())
        }

        val baos = ByteArrayOutputStream()
        selectedImage?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = imageRef?.putBytes(data)

        val urlTask = uploadTask?.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imageRef.downloadUrl
        }?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                updateDatabase(downloadUri)
            } else {
                Log.e(TAG, "ImageUpload:failed", task.exception)
            }
        }
    }

    private fun updateDatabase(downloadUri: Uri?) {
        val currentUser = Firebase.auth.currentUser!!
        val database = Firebase.database.reference

        val update = database.child("users").child(currentUser.uid).child("imageUrl")
            .setValue(downloadUri.toString())

        update.addOnSuccessListener {
            Toast.makeText(this, "Image changed", Toast.LENGTH_SHORT).show()
        }
    }


}