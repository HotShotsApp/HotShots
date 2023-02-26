package tw.app.hotshots.activity.notifications

/**
 * HotShots
 * NotificationsActivity.kt created at 23:45:15 23.02.2023
 */

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.launch
import tw.app.hotshots.R
import tw.app.hotshots.adapter.recyclerview.NotificationsAdapter
import tw.app.hotshots.adapter.recyclerview.OnNotificationClickListener
import tw.app.hotshots.database.notifications.DeleteNotification
import tw.app.hotshots.database.notifications.EditNotification
import tw.app.hotshots.database.notifications.GetNotifications
import tw.app.hotshots.database.notifications.NotificationSetting
import tw.app.hotshots.database.notifications.NotificationsDatabaseSettings
import tw.app.hotshots.database.notifications.OnNotificationsListListener
import tw.app.hotshots.databinding.ActivityNotificationsBinding
import tw.app.hotshots.model.main.Notification
import tw.app.hotshots.ui.loading.LoadingDialog

class NotificationsActivity : AppCompatActivity(), CoroutineScope {
    /* ------------------------------------------ */
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    /* ------------------------------------------ */

    private var _binding: ActivityNotificationsBinding? = null
    private val binding get() = _binding!!

    private var _adapter: NotificationsAdapter? = null
    private val adapter get() = _adapter!!

    private var _loading: LoadingDialog? = null
    private val loading get() = _loading!!

    private var _notificationsListener: OnNotificationsListListener? = null
    private val notificationsListener get() = _notificationsListener!!


    private var newNotificationCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setup()
    }

    private fun loadNotifications() {
        loading.show()

        launch {
            GetNotifications()
                .invoke(
                    NotificationsDatabaseSettings(NotificationSetting.ALL),
                    notificationsListener
                )
        }
    }

    private fun buildNotificationsListener() {
        _notificationsListener = object : OnNotificationsListListener {
            override fun onReceived(notifications: MutableList<Notification>) {
                runOnUiThread {
                    _adapter = NotificationsAdapter(notifications, this@NotificationsActivity)
                    binding.notificationsRecyclerView.adapter = adapter

                    setupNotificationClickEvent()

                    // COUNT NEW NOTIFICATIONS ->

                    for (notification in notifications) {
                        if (!notification.isMarkedAsRead)
                            newNotificationCount += 1
                    }

                    setActionBarCount(newNotificationCount)

                    // COUNT NEW NOTIFICATIONS <-

                    loading.dismiss()
                }
            }

            override fun onError(reason: String) {
                runOnUiThread {
                    Toast.makeText(this@NotificationsActivity, "Błąd: $reason", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    private fun setActionBarCount(amount: Int) {
        binding.toolbar.title = "Powiadomienia ($amount)"
    }

    private fun setupNotificationClickEvent() {
        adapter.setOnNotificationClickListener(object : OnNotificationClickListener {
            override fun onClick(notification: Notification, position: Int) {
                if (!notification.isMarkedAsRead) {
                    adapter.markAsRead(notification, position)

                    newNotificationCount -= 1
                    setActionBarCount(newNotificationCount)

                    launch {
                        EditNotification(notification).markAsRead()
                    }
                }
            }

            override fun onLongClickListener(notification: Notification, position: Int) {
                adapter.removeFromList(position)

                launch {
                    DeleteNotification()
                        .delete(notification.uid, null)
                }
            }
        })
    }

    private fun setup() {
        _loading = LoadingDialog(this@NotificationsActivity)
        loading.build()

        buildNotificationsListener()
        loadNotifications()

        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.delete_all_notifications_button) {
                adapter.removeAll()

                launch {
                    DeleteNotification()
                        .deleteAll(null)
                }
            }

            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}