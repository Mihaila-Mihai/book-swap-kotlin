package ro.example.bookswap

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import ro.example.bookswap.models.User
import java.lang.Exception
import kotlin.random.Random

private const val CHANNEL_ID = "my_channel"

class FirebaseService : FirebaseMessagingService() {

    companion object {
        var sharedPref: SharedPreferences? = null

        var token: String?
        get() {
            return sharedPref?.getString("token", "")
        }
        set(value) {
            sharedPref?.edit()?.putString("token", value)?.apply()
        }
    }

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        token = newToken
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val intent = Intent(this, MessageActivity::class.java)
        val user = message.data["title"]?.split('+')?.get(0)
        intent.putExtra("user", user)
        Log.d("FirebaseService", message.data["title"].toString())
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random.nextInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        Firebase.database.reference.child("users").child(user!!).get().addOnCompleteListener {
            val userObj = it.result.getValue<User>()
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_ONE_SHOT)


            Picasso.get().load(userObj?.imageUrl).into(object: Target {
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    val picture = NotificationCompat.BigPictureStyle().bigPicture(bitmap)
                    picture.setSummaryText("notification with picture")
                    val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                        .setContentTitle(message.data["title"]?.split('+')?.get(1))
                        .setContentText(message.data["message"])
                        .setSmallIcon(R.drawable.ic_user)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setLargeIcon(bitmap)
                        .build()
                    notificationManager.notify(notificationID, notification)
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                    TODO("Not yet implemented")
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                    Log.d("FirebaseService", "Preparing")
                }

            })



        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "channelName"
        val channel = NotificationChannel(CHANNEL_ID, channelName, IMPORTANCE_HIGH).apply {
            description = "My channel description"
            enableLights(true)
            lightColor = Color.GREEN
        }
        notificationManager.createNotificationChannel(channel)
    }

}