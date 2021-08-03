package ro.example.bookswap

import android.content.Intent
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.Explode
import android.transition.Fade
import android.transition.Transition
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import ro.example.bookswap.fragments.*

class MainActivity : AppCompatActivity(), ExitDialogFragment.NoticeDialogListener {

    private lateinit var navigationBarView: NavigationBarView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<DiscoverFragment>(R.id.fragment_container)
            }
        }

        with(window) {
            val requestFeature = requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            exitTransition = Explode()
            enterTransition = Explode()
        }

        setContentView(R.layout.activity_main)

        val toast: Toast = Toast.makeText(applicationContext, "text", Toast.LENGTH_SHORT)


        navigationBarView = findViewById(R.id.bottom_nav)

        navigationBarView.setOnItemSelectedListener { item ->

            when (item.itemId) {
                R.id.page_1 -> {
                    toast.setText("Page 1")
                    toast.show()
                    supportFragmentManager.commit {
                        replace<DiscoverFragment>(R.id.fragment_container)
                        setReorderingAllowed(true)
//                        addToBackStack("page_1")
                    }

                    true
                }
                R.id.page_2 -> {
                    toast.setText("Page 2")
                    toast.show()
                    supportFragmentManager.commit {
                        replace<LikesFragment>(R.id.fragment_container)
                        setReorderingAllowed(true)
//                        addToBackStack("page_2")
                    }
                    true
                }
                R.id.page_3 -> {
                    toast.setText("Page 3")
                    toast.show()
                    supportFragmentManager.commit {
                        replace<ChatsFragment>(R.id.fragment_container)
                        setReorderingAllowed(true)
//                        addToBackStack("page_3")
                    }
                    true
                }
                R.id.page_4 -> {
                    toast.setText("Page 4")
                    toast.show()
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

    override fun onBackPressed() {
        val discover: DiscoverFragment? =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as? DiscoverFragment
        if (discover == null) {
            Toast.makeText(applicationContext, "Back pressed", Toast.LENGTH_SHORT).show()
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
}
