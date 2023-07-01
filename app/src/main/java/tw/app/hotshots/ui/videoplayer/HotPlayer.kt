package tw.app.hotshots.ui.videoplayer

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import tw.app.hotshots.databinding.HotPlayerViewBinding
import java.util.Formatter
import java.util.Locale


class HotPlayer(
    context: Context,
    attrs: AttributeSet
) : FrameLayout(context, attrs) {
    private var _binding: HotPlayerViewBinding? = null
    private val binding get() = _binding!!

    private var _onControllerAction: OnControllerAction? = null
    private val onControllerAction get() = _onControllerAction!!

    private var _exoPlayer: ExoPlayer? = null
    private val exoPlayer get() = _exoPlayer!!
    private var _playerView: StyledPlayerView? = null
    private val playerView get() = _playerView!!

    private var _playerListener: Player.Listener? = null
    private val playerListener get() = _playerListener!!

    private var _currentVideo: MediaItem? = null
    private val currentVideo get() = _currentVideo!!

    private var isSlowMotionEnabled = false

    private val POSITION_CHECK_INTERVAL: Long = 600

    init {
        _binding = HotPlayerViewBinding.inflate(LayoutInflater.from(context), this, true)

        _playerView = binding.playerView

        initPlayer()
        initController()
    }

    private fun initPlayer() {
        _exoPlayer = ExoPlayer.Builder(context).build()

        if (playerView.player == null)
            playerView.player = exoPlayer

        exoPlayer.volume = 0f

        _playerListener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)

                if (playbackState == Player.STATE_READY) {
                    binding.playerController.toggleError("", false)
                    binding.playerController.updateVideoTimeText("00:00 / ${stringForTime(exoPlayer.duration.toInt())}")
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)

                binding.playerController.toggleError(
                    "Kod: ${error.errorCode}\nNazwa: ${error.errorCodeName}\nTreść błędu: ${error.message.toString()}"
                )
            }

            override fun onIsLoadingChanged(isLoading: Boolean) {
                super.onIsLoadingChanged(isLoading)

                binding.playerController.toggleLoading(isLoading)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)

                if (isPlaying)
                    playerView.postDelayed({ getCurrentPlayerPosition() }, POSITION_CHECK_INTERVAL)
            }
        }

        exoPlayer.addListener(playerListener)
    }

    private fun getCurrentPlayerPosition() {
        binding.playerController.updateVideoTimeText("${stringForTime(exoPlayer.currentPosition.toInt())} / ${stringForTime(exoPlayer.duration.toInt())}")
        if (exoPlayer.isPlaying) {
            playerView.postDelayed({ getCurrentPlayerPosition() }, POSITION_CHECK_INTERVAL)
        }
    }

    private fun initController() {
        _onControllerAction = object : OnControllerAction {
            override fun onPlay() {
                play()
            }

            override fun onPause() {
                pause()
            }

            override fun onSlowMotion() {
                isSlowMotionEnabled = !isSlowMotionEnabled

                if (isSlowMotionEnabled)
                    exoPlayer.setPlaybackSpeed(0.3f)
                else
                    exoPlayer.setPlaybackSpeed(1f)
            }

            override fun onToggleAudio() {
                if (exoPlayer.volume == 0f) {
                    exoPlayer.volume = 1f
                    binding.playerController.toggleAudioIcon(true)
                } else {
                    exoPlayer.volume = 0f
                    binding.playerController.toggleAudioIcon(false)
                }
            }
        }

        binding.playerController.setOnControllerActionListener(onControllerAction)
    }

    public fun loadFromURL(urlToLoad: String, autoPlay: Boolean) {
        // ExoPlayer doesn't allow to load videos from not secured websites | HTTP |
        var url = urlToLoad
        if (!url.contains("https")) {
            if (url.contains("http")) {
                url = url.replace("http", "https")
            }
        }

        _currentVideo = MediaItem.fromUri(url)
        exoPlayer.setMediaItem(currentVideo)
        exoPlayer.prepare()

        if (autoPlay) {
            play()
            binding.playerController.changeButtonState(true)
        }
    }

    public fun play() {
        if (_currentVideo != null && !exoPlayer.isPlaying) {
            exoPlayer.play()
        }
    }

    public fun pause() {
        if (_currentVideo != null && exoPlayer.isPlaying) {
            exoPlayer.pause()
        }
    }

    public fun stop() {
        if (_currentVideo != null) {
            exoPlayer.stop()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        exoPlayer.release()

        _binding = null
        _exoPlayer = null
        _playerView = null
        _currentVideo = null
    }

    private fun stringForTime(timeMs: Int): String {
        val mFormatBuilder = StringBuilder()
        val mFormatter = Formatter(mFormatBuilder, Locale.getDefault())
        val totalSeconds = timeMs / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        mFormatBuilder.setLength(0)
        return if (hours > 0) {
            mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            mFormatter.format("%02d:%02d", minutes, seconds).toString()
        }
    }
}