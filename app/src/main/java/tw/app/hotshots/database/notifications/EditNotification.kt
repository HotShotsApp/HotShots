package tw.app.hotshots.database.notifications

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.database.user.UserSingleton
import tw.app.hotshots.model.main.Notification
import tw.app.hotshots.util.ConstantsDatabaseCollections

class EditNotification(
    private val notification: Notification
) {
    suspend fun markAsRead() {
        notification.isMarkedAsRead = true

        FirebaseFirestore
            .getInstance()
            .collection(ConstantsDatabaseCollections.USERS)
            .document(UserSingleton.instance?.user!!.uid)
            .collection(ConstantsDatabaseCollections.NOTIFICATIONS)
            .document(notification.uid)
            .set(notification)
            .await()
    }
}