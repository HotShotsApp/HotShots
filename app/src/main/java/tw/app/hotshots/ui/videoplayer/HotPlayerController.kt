package tw.app.hotshots.ui.videoplayer

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewPropertyAnimator
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import tw.app.hotshots.R
import tw.app.hotshots.databinding.HotPlayerControllsBinding
import tw.app.hotshots.R.drawable as Images

class HotPlayerController(
    context: Context,
    attrs: AttributeSet
) : FrameLayout(context, attrs) {
    private var _binding: HotPlayerControllsBinding? = null
    private val binding get() = _binding!!

    private var _listener: OnControllerAction? = null
    private val listener get() = _listener!!

    private var isPlaying = false

    private var rootAnimator: ViewPropertyAnimator

    // Constants
    private val SHOW_CONTROLLER_ANIMATION_DURATION: Long = 800
    private val HIDE_CONTROLLER_ANIMATION_DURATION: Long = 800

    init {
        _binding = HotPlayerControllsBinding.inflate(LayoutInflater.from(context), this, true)

        rootAnimator = binding.root.animate()

        setup()
        setupControllerVisibility()
    }

    private fun setup() {
        binding.playPauseButton.setOnClickListener {
            if (isPlaying) { // If playing - stop
                changeButtonState(false)
                listener.onPause()
            } else { // If paused - play
                changeButtonState(true)
                listener.onPlay()
            }
        }

        binding.slowMotionButton.setOnClickListener {
            listener.onSlowMotion()
        }
        binding.audioToggleButton.setOnClickListener {
            listener.onToggleAudio()
        }
    }

    private fun setupControllerVisibility() {
        binding.root.setOnClickListener {
            if (!isControllerVisible()) {
                showController()
            } else {
                hideController()
            }
        }
    }

    private fun hideController() {
        rootAnimator.cancel()
        rootAnimator.alpha(0.0f).setDuration(HIDE_CONTROLLER_ANIMATION_DURATION).start()
    }

    private fun showController() {
        rootAnimator.cancel()
        rootAnimator.alpha(1.0f).setDuration(SHOW_CONTROLLER_ANIMATION_DURATION).start()
    }

    private fun isControllerVisible(): Boolean {
        return binding.root.alpha == 1.0f
    }

    public fun toggleLoading(visible: Boolean) {
        binding.loadingIndicator.isVisible = visible
    }

    public fun changeButtonState(isPlayerPlaying: Boolean) {
        isPlaying = isPlayerPlaying

        if (isPlaying) {
            binding.playPauseButton.setIconResource(Images.pause)
        } else {
            binding.playPauseButton.setIconResource(Images.play)
        }
    }

    public fun toggleAudioIcon(isAudioOn: Boolean) {
        if (isAudioOn)
            binding.audioToggleButton.icon = AppCompatResources.getDrawable(context, R.drawable.volume_on)
        else
            binding.audioToggleButton.icon = AppCompatResources.getDrawable(context, R.drawable.volume_off)
    }

    public fun toggleError(error: String, isVisible: Boolean = true) {
        binding.errorCard.isVisible = isVisible
        binding.videoTimeText.isVisible = !isVisible
        binding.slowMotionButton.isVisible = !isVisible
        binding.loadingIndicator.isVisible = !isVisible
        binding.audioToggleButton.isVisible = !isVisible
        binding.errorDetailsText.text = error
    }

    public fun setOnControllerActionListener(_newListener: OnControllerAction) {
        _listener = _newListener
    }

    public fun updateVideoTimeText(text: String) {
        binding.videoTimeText.text = text
    }
}

interface OnControllerAction {
    fun onPlay()

    fun onPause()

    fun onSlowMotion()

    fun onToggleAudio()
}