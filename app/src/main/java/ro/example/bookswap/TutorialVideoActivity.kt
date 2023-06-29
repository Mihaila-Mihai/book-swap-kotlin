package ro.example.bookswap

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class TutorialVideoActivity : AppCompatActivity() {

    // declaring a null variable for VideoView
    var videoView: VideoView? = null

    // declaring a null variable for MediaController
    var mediaControls: MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial_video)


        // assigning id of VideoView from
        // tutorial_video.xml layout file
        videoView = findViewById<View>(R.id.videoViewId) as VideoView

        if (mediaControls == null) {
            // creating an object of media controller class
            mediaControls = MediaController(this)

            // set the anchor view for the video view
            mediaControls!!.setAnchorView(this.videoView)
        }

        // set the media controller for video view
        videoView!!.setMediaController(mediaControls)

        // set the absolute path of the video file which is going to be played
        videoView!!.setVideoURI(
            Uri.parse("android.resource://"
                + packageName + "/" + R.raw.example))

        videoView!!.requestFocus()

        // starting the video
        videoView!!.start()


        // display a toast message if any
        // error occurs while playing the video
        videoView!!.setOnErrorListener { mp, what, extra ->
            Toast.makeText(applicationContext, "An error occurred " +
                    "while playing the video", Toast.LENGTH_LONG).show()
            false
        }
    }
}