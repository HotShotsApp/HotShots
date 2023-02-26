package tw.app.hotshots.database.notifications

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.model.main.ExtraType
import tw.app.hotshots.model.main.Notification
import tw.app.hotshots.util.ConstantsDatabaseCollections
import tw.app.hotshots.util.ConstantsDatabaseDocuments

class SendNotification {

    interface OnNotificationSendListener {
        fun onSent()

        fun onError(reason: String)
    }

    companion object {
        /**
         * Send Notification to user
         *
         * @param targetUserUid [String] User UID indicating to which user notification should be send
         * @param notification [Notification] instance with notification details
         * @param listener [OnNotificationSendListener] instance to notify when notification is sent or error occurs
         */
        suspend fun send(targetUserUid: String, notification: Notification, listener: OnNotificationSendListener) {
            FirebaseFirestore
                .getInstance()
                .collection(ConstantsDatabaseCollections.USERS)
                .document(targetUserUid)
                .collection(ConstantsDatabaseCollections.NOTIFICATIONS)
                .document(notification.uid)
                .set(notification)
                .addOnFailureListener {
                    listener.onError(it.message.toString())
                }
                .await()

            listener.onSent()
        }

        fun fastDefaultNotification(title: String, message: String): Notification {
            return Notification(
                title = title,
                message = message
            )
        }

        suspend fun SendInfluSentNotification(userUID: String, influencerNick: String, listener: OnNotificationSendListener?) {
            val notification = Notification(
                title = "Wysłano zgłoszenie!",
                message = "Twoje zgłoszenie o dodanie influencera \"$influencerNick\" zostało wysłane."
            )

            FirebaseFirestore.getInstance()
                .collection(ConstantsDatabaseCollections.USERS)
                .document(userUID)
                .collection(ConstantsDatabaseCollections.NOTIFICATIONS)
                .document(notification.uid)
                .set(notification)
                .addOnFailureListener {
                    listener?.onError(it.message.toString())
                }
                .await()

            listener?.onSent()
        }

        suspend fun SendInfluAcceptedNotification(userUID: String, influencerNick: String, influencerUid: String, listener: OnNotificationSendListener?) {
            val notification = Notification(
                title = "Zgłoszenie zostało przyjęte!",
                message = "Twoje zgłoszenie o dodanie influencera \"$influencerNick\" zostało przyjęte.",
                containsExtras = true,
                extraType = ExtraType.OPEN_INFLUENCER,
                extraValue = influencerUid
            )

            FirebaseFirestore.getInstance()
                .collection(ConstantsDatabaseCollections.USERS)
                .document(userUID)
                .collection(ConstantsDatabaseCollections.NOTIFICATIONS)
                .document(notification.uid)
                .set(notification)
                .addOnFailureListener {
                    listener?.onError(it.message.toString())
                }
                .await()

            listener?.onSent()
        }

        suspend fun SendInfluDeclinedNotification(userUID: String, influencerNick: String, declineReason: String, listener: OnNotificationSendListener?) {
            val notification = Notification(
                title = "Zgłoszenie zostało odrzucone!",
                message = "Twoje zgłoszenie o dodanie influencera \"$influencerNick\" zostało odrzucone.",
                containsExtras = true,
                extraType = ExtraType.OPEN_DIALOG,
                extraValue = "Powód:\n$declineReason"
            )

            FirebaseFirestore.getInstance()
                .collection(ConstantsDatabaseCollections.USERS)
                .document(userUID)
                .collection(ConstantsDatabaseCollections.NOTIFICATIONS)
                .document(notification.uid)
                .set(notification)
                .addOnFailureListener {
                    listener?.onError(it.message.toString())
                }
                .await()

            listener?.onSent()
        }
    }
}