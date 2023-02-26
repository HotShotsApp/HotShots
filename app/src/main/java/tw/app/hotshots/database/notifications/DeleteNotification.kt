package tw.app.hotshots.database.notifications

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.database.user.UserSingleton
import tw.app.hotshots.util.ConstantsDatabaseCollections

class DeleteNotification {
    suspend fun delete(notificationUid: String, listener: OnNotificationDelete?) {
        val notificationsDocuments = FirebaseFirestore
            .getInstance()
            .collection(ConstantsDatabaseCollections.USERS)
            .document(UserSingleton.instance?.user!!.uid)
            .collection(ConstantsDatabaseCollections.NOTIFICATIONS)
            .document(notificationUid)
            .delete()
            .addOnSuccessListener {
                listener?.onDeleted()
            }
            .addOnFailureListener {
                listener?.onError(it.message.toString())
            }
            .await()
    }
    suspend fun deleteAll(listener: OnNotificationDelete?) {
        val notificationsDocuments = FirebaseFirestore
            .getInstance()
            .collection(ConstantsDatabaseCollections.USERS)
            .document(UserSingleton.instance?.user!!.uid)
            .collection(ConstantsDatabaseCollections.NOTIFICATIONS)
            .get()
            .addOnFailureListener {
                listener?.onError(it.message.toString())
            }
            .await()

        for (notification in notificationsDocuments) {
            notification
                .reference
                .delete()
                .await()
        }

        listener?.onDeleted()
    }

    interface OnNotificationDelete {
        fun onDeleted()

        fun onError(reason: String)
    }
}