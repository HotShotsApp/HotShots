package tw.app.hotshots.ui.video

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import tw.app.hotshots.databinding.ViewPlayerBinding

class PlayerView(
    context: Context,
    attrs: AttributeSet
) : FrameLayout(context, attrs) {
    private var _binding: ViewPlayerBinding? = null
    private val binding get() = _binding!!

    init {
        _binding = ViewPlayerBinding.inflate(LayoutInflater.from(context), this, true)
    }

    public fun setVideoUrl(videoUrl: String) {

    }

    public fun play() {

    }

    public fun pause() {

    }
}