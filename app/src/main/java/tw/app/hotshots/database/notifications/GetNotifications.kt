package tw.app.hotshots.database.notifications

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.database.user.UserSingleton
import tw.app.hotshots.model.main.Notification
import tw.app.hotshots.util.ConstantsDatabaseCollections

class GetNotifications {
    suspend fun invoke(settings: NotificationsDatabaseSettings, listener: OnNotificationsListListener) {
        if (settings.get() == NotificationSetting.ALL) {
            getAllNotifications(listener)
        }
    }

    private suspend fun getAllNotifications(listener: OnNotificationsListListener) {
        val database = FirebaseFirestore.getInstance().collection(ConstantsDatabaseCollections.USERS).document(UserSingleton.instance?.user!!.uid).collection("notifications")

        val notificationsQuery = database
            .get()
            .addOnFailureListener {
                listener.onError(it.message.toString())
            }
            .await()

        if (notificationsQuery.isEmpty) {
            listener.onReceived(arrayListOf())
            return
        }

        val notificationList: MutableList<Notification> = arrayListOf()
        for (notificationDocument in notificationsQuery.documents) {
            val notification = notificationDocument.toObject(Notification::class.java)

            if (notification != null)
                notificationList.add(notification)
        }

        listener.onReceived(notificationList)
    }
}

interface OnNotificationsListListener {
    fun onReceived(notifications: MutableList<Notification>)

    fun onError(reason: String)
}

interface OnNotificationAllTimeListener {
    fun onNewNotification(amount: Int)

    fun onError()
}

class NotificationsDatabaseSettings(
    setting: NotificationSetting
) {
    private var _setting: NotificationSetting = NotificationSetting.ALL

    init {
        _setting = setting
    }

    fun get(): NotificationSetting {
        return _setting
    }
}

enum class NotificationSetting {
    NOT_READED,
    READED,
    ALL
}