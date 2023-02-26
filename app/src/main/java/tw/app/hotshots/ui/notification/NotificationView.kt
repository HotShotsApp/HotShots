package tw.app.hotshots.ui.notification

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import tw.app.hotshots.databinding.ItemNotificationBinding
import tw.app.hotshots.model.main.ExtraType
import tw.app.hotshots.model.main.Notification
import tw.app.hotshots.util.TimeUtil

class NotificationView(
    context: Context,
    attributeSet: AttributeSet?,
    defStyle: Int
) : FrameLayout(context, attributeSet, defStyle) {

    constructor(context: Context) : this(context, null, 0) {}
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}

    private var binding: ItemNotificationBinding
    private var listener: OnReasonButtonClickListener? = null

    init {
        binding = ItemNotificationBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun setup(notification: Notification) {
        setNotificationTitle(notification.title)
        setNotificationMessage(notification.message)
        setNotificationCreatedAt(notification.createdAt)
        setMarked(notification.isMarkedAsRead)

        if (notification.containsExtras) {
            if (notification.extraType == ExtraType.OPEN_DIALOG) {
                setNotificationOpenDialog()
            }
        }
    }

    private fun setNotificationTitle(title: String) {
        binding.notificationTitleTextView.text = title
    }

    private fun setNotificationMessage(content: String) {
        binding.notificationContentTextView.text = content
    }

    private fun setNotificationOpenDialog() {
        binding.reasonButton.visibility = VISIBLE

        binding.reasonButton.setOnClickListener {
            if (isOnReasonButtonClickListenerAvailable())
                listener!!.onClick()
        }
    }

    private fun setNotificationHidden() {

    }

    private fun setNotificationCreatedAt(time: Long) {
        binding.notificationCreatedAtTextView.text = TimeUtil.convertLongToTime(time)
    }

    private fun setMarked(value: Boolean) {
        if (value) {
            binding.notificationCard.strokeWidth = 0
        } else {
            binding.notificationCard.strokeWidth = 2
        }
    }

    private fun isOnReasonButtonClickListenerAvailable(): Boolean {
        return listener != null
    }

    fun setOnReasonButtonClickListener(_listener: OnReasonButtonClickListener) {
        listener = _listener
    }

    interface OnReasonButtonClickListener {
        fun onClick()
    }
}