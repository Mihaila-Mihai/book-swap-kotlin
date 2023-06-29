package ro.example.bookswap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class TutorialYtVideoActivity : AppCompatActivity() {
    // on the below line we are creating a variable
    // for our youtube player view.
    lateinit var youtubePlayerView: YouTubePlayerView

    // on below line we are creating a
    // string variable for our video id.
    var videoID = "Xtc09mpzVHM"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // on below line we are setting
        // screen orientation to landscape
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        // on below line we are setting flags to
        // change our activity to full screen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_tutorial_video_yt)

        // on below line we are hiding our action bar
        actionBar?.hide()

        // on below line we are initializing
        // our youtube player view with its id.
        youtubePlayerView = findViewById(R.id.youTubePlayerView)
        // on below line we are setting full
        // screen for our youtube player view.
        youtubePlayerView.enterFullScreen()
        youtubePlayerView.toggleFullScreen()

        // on below line we are getting observer
        // for our youtube player view.
        lifecycle.addObserver(youtubePlayerView)

        // on below line we are getting
        // youtube player controller ui.
        youtubePlayerView.getPlayerUiController()

        // on below line we are
        // entering it to full screen.
        youtubePlayerView.enterFullScreen()
        youtubePlayerView.toggleFullScreen()

        // on below line we are adding listener
        // for our youtube player view.
        youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                // loading the selected video
                // into the YouTube Player
                youTubePlayer.loadVideo(videoID, 0f)
            }

            override fun onStateChange(
                youTubePlayer: YouTubePlayer,
                state: PlayerConstants.PlayerState
            ) {
                // this method is called if video has ended,
                super.onStateChange(youTubePlayer, state)
            }
        })
    }
}