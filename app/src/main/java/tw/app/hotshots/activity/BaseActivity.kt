package tw.app.hotshots.activity

import android.view.KeyEvent
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import tw.app.hotshots.extensions.hideKeyboard
import kotlin.coroutines.CoroutineContext

open class BaseActivity : AppCompatActivity(), CoroutineScope {
    /* ------------------------------------------ */
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    /* ------------------------------------------ */

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        hideKeyboard(ev)
        return super.dispatchTouchEvent(ev)
    }
}