package tw.app.hotshots.ui.button

import android.animation.Animator
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewPropertyAnimator
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import com.google.rpc.context.AttributeContext.Resource
import tw.app.hotshots.databinding.ViewTopButtonBinding

class TopButtonView(
    context: Context,
    attrs: AttributeSet
) : FrameLayout(context, attrs) {
    private var _binding: ViewTopButtonBinding? = null
    private val binding get() = _binding!!

    private var _rootAnimator: ViewPropertyAnimator? = null
    private val rootAnimator get() = _rootAnimator!!

    private var _showAnimatorListener: Animator.AnimatorListener? = null
    private val showAnimatorListener get() = _showAnimatorListener!!

    private var _hideAnimatorListener: Animator.AnimatorListener? = null
    private val hideAnimatorListener get() = _hideAnimatorListener!!

    private var _clickListener: ClickListener? = null
    private val clickListener get() = _clickListener!!

    private var isShowing = false
    private var shouldAnimate = true

    private var ANIMATION_DURATION: Long = 500

    init {
        _binding = ViewTopButtonBinding.inflate(LayoutInflater.from(context), this, true)

        _rootAnimator = binding.root.animate()

        setup()
    }

    private fun setup() {
        binding.actionButton.setOnClickListener {
            if (_clickListener != null) {
                clickListener.onClick()
            }
        }

        /** ANIMATIONS LISTENERS **/
        _showAnimatorListener = object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {
                isShowing = true
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        }

        _hideAnimatorListener = object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {
                isShowing = false
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        }
    }

    public fun show() {
        if (isShowing)
            return

        if (shouldAnimate)
            rootAnimator.translationY(0f).alpha(1f).setDuration(ANIMATION_DURATION).setListener(showAnimatorListener).start()
        else {
            binding.root.translationY = 0f
            binding.root.alpha = 1f
            isShowing = true
        }
    }

    public fun hide() {
        if (!isShowing)
            return

        if (shouldAnimate)
            rootAnimator.translationY(-100f).alpha(0f).setDuration(ANIMATION_DURATION).setListener(hideAnimatorListener).start()
        else {
            binding.root.translationY = -100f
            binding.root.alpha = 0f
            isShowing = false
        }
    }

    public fun setAnimationDuration(duration: Long) {
        ANIMATION_DURATION = duration
    }

    public fun setAnimate(animate: Boolean = true) {
        shouldAnimate = animate
    }

    public fun setText(text: String) {
        binding.actionButton.text = text
    }

    public fun setIcon(icon: Int?) {
        if (icon == null) {
            binding.actionButton.icon = null
            return
        }

        binding.actionButton.icon = AppCompatResources.getDrawable(context, icon)
    }

    public fun setIcon(icon: Drawable?) {
        if (icon == null) {
            binding.actionButton.icon = null
            return
        }

        binding.actionButton.icon = icon
    }

    public fun isShowing(): Boolean {
        return isShowing
    }

    public fun setOnClickListener(_listener: ClickListener) {
        _clickListener = _listener
    }

    public interface ClickListener {
        fun onClick()
    }
}