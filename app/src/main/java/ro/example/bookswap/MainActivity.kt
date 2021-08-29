package ro.example.bookswap

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.transition.Explode
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import ro.example.bookswap.fragments.*
import ro.example.bookswap.models.LocationModel
import java.util.jar.Manifest

const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), ExitDialogFragment.NoticeDialogListener, EasyPermissions.PermissionCallbacks {


    companion object {
        const val PERMISSION_LOCATION_REQUEST_CODE = 1
    }

    private val currentUser = Firebase.auth.currentUser?.uid
    private lateinit var navigationBarView: NavigationBarView
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<DiscoverFragment>(R.id.fragment_container)
            }
        }
        FirebaseService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            FirebaseService.token = token
            Firebase.database.reference.child("users").child(currentUser!!).child("token").setValue(token)
        }

        with(window) {
            val requestFeature = requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            exitTransition = Explode()
            enterTransition = Explode()
        }

        setContentView(R.layout.activity_main)

//        val toast: Toast = Toast.makeText(applicationContext, "text", Toast.LENGTH_SHORT)


        try {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        } catch (e: Exception) {
            Log.e(TAG, "${e.message}")
        }

        // location permission
        if (hasLocationPermission()) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                Log.d("Location latitude", location.latitude.toString())
                Log.d("Location longitude", location.longitude.toString())

                Firebase.database.reference.child("users").child(currentUser!!).child("location").setValue(LocationModel(
                    location.longitude.toString(),
                    location.latitude.toString()
                )).addOnFailureListener {
                    Toast.makeText(this, "Location update failed", Toast.LENGTH_SHORT).show()
                }

            }
        } else {
            requestLocationPermission()
        }

        navigationBarView = findViewById(R.id.bottom_nav)

        navigationBarView.setOnItemSelectedListener { item ->

            when (item.itemId) {
                R.id.page_1 -> {
//                    toast.setText("Page 1")
//                    toast.show()
                    supportFragmentManager.commit {
                        replace<DiscoverFragment>(R.id.fragment_container)
                        setReorderingAllowed(true)
//                        addToBackStack("page_1")
                    }

                    true
                }
                R.id.page_2 -> {
//                    toast.setText("Page 2")
//                    toast.show()
                    supportFragmentManager.commit {
                        replace<NotificationsFragment>(R.id.fragment_container)
                        setReorderingAllowed(true)
//                        addToBackStack("page_2")
                    }
                    true
                }
                R.id.page_3 -> {
//                    toast.setText("Page 3")
//                    toast.show()
                    supportFragmentManager.commit {
                        replace<ChatsFragment>(R.id.fragment_container)
                        setReorderingAllowed(true)
//                        addToBackStack("page_3")
                    }
                    true
                }
                R.id.page_4 -> {
//                    toast.setText("Page 4")
//                    toast.show()
                    supportFragmentManager.commit {
                        replace<ProfileFragment>(R.id.fragment_container)
                        setReorderingAllowed(true)
//                        addToBackStack("page_4")
                    }

                    true
                }
                else -> false
            }
        }

    }

    private fun hasLocationPermission() =
        EasyPermissions.hasPermissions(
            context = this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )

    private fun requestLocationPermission() {
        EasyPermissions.requestPermissions(
            this,
            "This application cannot work without Location Permission.",
            PERMISSION_LOCATION_REQUEST_CODE,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onBackPressed() {
        val discover: DiscoverFragment? =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as? DiscoverFragment
        if (discover == null) {
//            Toast.makeText(applicationContext, "Back pressed", Toast.LENGTH_SHORT).show()
            supportFragmentManager.commit {
                replace<DiscoverFragment>(R.id.fragment_container)
                setReorderingAllowed(true)
            }
            navigationBarView.selectedItemId = R.id.page_1
        } else {
//            super.onBackPressed()
            showDialog()
        }
    }

    private fun showDialog() {
        val dialog = ExitDialogFragment()
        dialog.show(supportFragmentManager, "ExitDialogFragment")
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        super.onBackPressed()
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        dialog.dismiss()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(this).build().show()
        } else {
            requestLocationPermission()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show()
    }
}
