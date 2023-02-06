package tw.app.hotshots.ui.imageview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.widget.ImageView

@SuppressLint("AppCompatCustomView")
class TopCropImageView(context: Context?, attrs: AttributeSet?, defStyle: Int) :
    ImageView(context, attrs, defStyle) {
    private val mMatrix: Matrix
    private var mHasFrame = false

    constructor(context: Context?) : this(context, null, 0) {}
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0) {}

    init {
        mMatrix = Matrix()
        // we have to use own matrix because:
        // ImageView.setImageMatrix(Matrix matrix) will not call
        // configureBounds(); invalidate(); because we will operate on ImageView object
    }

    protected override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        val changed: Boolean = super.setFrame(l, t, r, b)
        if (changed) {
            mHasFrame = true
            // we do not want to call this method if nothing changed
            setupScaleMatrix(r - l, b - t)
        }
        return changed
    }

    private fun setupScaleMatrix(width: Int, height: Int) {
        if (!mHasFrame) {
            // we have to ensure that we already have frame
            // called and have width and height
            return
        }
        val drawable: Drawable = getDrawable()
            ?: // we have to check if drawable is null because
            // when not initialized at startup drawable we can
            // rise NullPointerException
            return
        val matrix: Matrix = mMatrix
        val intrinsicWidth: Int = drawable.getIntrinsicWidth()
        val intrinsicHeight: Int = drawable.getIntrinsicHeight()
        val factorWidth = width / intrinsicWidth.toFloat()
        val factorHeight = height / intrinsicHeight.toFloat()
        val factor = Math.max(factorHeight, factorWidth)

        // there magic happen and can be adjusted to current
        // needs
        matrix.setTranslate(-intrinsicWidth / 2.0f, 0F)
        matrix.postScale(factor, factor, 0F, 0F)
        matrix.postTranslate(width / 2.0f, 0F)
        setImageMatrix(matrix)
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        // We have to recalculate image after chaning image
        setupScaleMatrix(getWidth(), getHeight())
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        // We have to recalculate image after chaning image
        setupScaleMatrix(getWidth(), getHeight())
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        // We have to recalculate image after chaning image
        setupScaleMatrix(getWidth(), getHeight())
    } // We do not have to overide setImageBitmap because it calls
    // setImageDrawable method
}