package tw.app.hotshots.adapter.recyclerview

import tw.app.hotshots.model.main.Notification
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tw.app.hotshots.databinding.ItemNotificationListBinding
import tw.app.hotshots.model.main.ExtraType
import tw.app.hotshots.ui.notification.NotificationView

/**
 * Adapter that shows available Posts
 *
 * @param mData MutableList with Posts
 */
class NotificationsAdapter(
    private var mData: MutableList<Notification>,
    private val context: Context
) : RecyclerView.Adapter<NotificationsAdapter.ViewHolder>() {

    private var onNotificationClickListener: OnNotificationClickListener? = null

    inner class ViewHolder(val binding: ItemNotificationListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationsAdapter.ViewHolder {
        val binding =
            ItemNotificationListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val notification = mData[position]
        val binding = holder.binding

        binding.notificationView.setup(notification)

        binding.notificationView.setOnClickListener {
            if (isClickListenerAvailable())
                onNotificationClickListener!!.onClick(notification, position)
        }

        binding.notificationView.setOnLongClickListener {
            if (isClickListenerAvailable())
                onNotificationClickListener!!.onLongClickListener(notification, position)

            true
        }

        if (notification.containsExtras) {
            if (notification.extraType == ExtraType.OPEN_DIALOG) {
                binding.notificationView.setOnReasonButtonClickListener(object : NotificationView.OnReasonButtonClickListener {
                    override fun onClick() {
                        // Trigger onClick to flag notification as read
                        onNotificationClickListener!!.onClick(notification, position)

                        // Show dialog
                        MaterialAlertDialogBuilder(context)
                            .setTitle("Powód")
                            .setMessage(notification.extraValue)
                            .setPositiveButton("Zamknij", null)
                            .show()
                    }
                })
            }
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    fun removeFromList(position: Int) {
        mData.removeAt(position)
        notifyItemRemoved(position)
    }

    fun removeAll() {
        mData = arrayListOf()
        notifyDataSetChanged()
    }

    fun markAsRead(notification: Notification, position: Int) {
        notification.isMarkedAsRead = true

        mData[position] = notification
        notifyItemChanged(position)
    }

    fun setOnNotificationClickListener(_listener: OnNotificationClickListener) {
        onNotificationClickListener = _listener
    }

    private fun isClickListenerAvailable(): Boolean {
        return onNotificationClickListener != null
    }

}

interface OnNotificationClickListener {
    fun onClick(notification: Notification, position: Int) {}

    fun onLongClickListener(notification: Notification, position: Int) {}

    fun onDoubleClick(notification: Notification, position: Int) {}
}