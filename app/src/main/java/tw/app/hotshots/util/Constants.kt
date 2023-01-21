package tw.app.hotshots.util

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import tw.app.hotshots.R

object Constants {
    val BANNED_USER_BORDER = Color.RED
    fun DEFAULT_USER_BORDER(context: Context): Int {
        return ContextCompat.getColor(context, R.color.user_default)
    }
    fun PREMIUM_USER_BORDER(context: Context): Int {
        return ContextCompat.getColor(context, R.color.user_premium)
    }
    fun MOD_TOP_USER_BORDER(context: Context): Int {
        return ContextCompat.getColor(context, R.color.user_moderator_top)
    }
    fun MOD_BOTTOM_USER_BORDER(context: Context): Int {
        return ContextCompat.getColor(context, R.color.user_moderator_bottom)
    }
    fun ADMIN_USER_BORDER(context: Context): Int {
        return ContextCompat.getColor(context, R.color.user_administrator)
    }

    val POST_IMAGE_RESIZE_VALUE = 450
}