package tw.app.hotshots.database.ban

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.Constants
import tw.app.hotshots.authentication.model.HotUser
import tw.app.hotshots.database.users.GetUser
import tw.app.hotshots.database.users.GetUserListener
import tw.app.hotshots.singleton.UserSingleton
import tw.app.hotshots.util.TimeUtil
import kotlin.Exception

class UserBan(
    var listener: UserBanListener
) {
    public suspend fun Ban(
        uid: String,
        banDuration: Long,
        banReason: String,
    ) {
        var isLastActionSuccess = true
        var lastException = Exception("Unknown Error!")

        val databaseUser = FirebaseFirestore
            .getInstance()
            .collection(Constants.Collections.USERS)
            .document(uid)
            .get()
            .addOnFailureListener {
                isLastActionSuccess = false
                lastException = it
            }
            .await()

        if (!isLastActionSuccess) {
            listener.onError(lastException)
            return
        }

        if (!databaseUser.exists()) {
            listener.onError(Exception("Użytkownik nie istnieje w bazie danych!"))
            return
        }

        val userToBan = databaseUser.toObject(HotUser::class.java)

        if (userToBan != null) {
            userToBan.isBanned = true
            userToBan.bannedTo = TimeUtil.currentTimeToLong() + banDuration
            userToBan.banReason = banReason
            userToBan.bannedBy = UserSingleton.instance?.user?.uid ?: "Error getting UID."

            FirebaseFirestore
                .getInstance()
                .collection(Constants.Collections.USERS)
                .document(uid)
                .set(userToBan)
                .addOnSuccessListener {
                    listener.onBanned()
                }
                .addOnFailureListener {
                    listener.onError(it)
                }
                .await()
        } else {
            listener.onError(Exception("Cannot convert document into HotUser object!"))
            return
        }
    }

    public suspend fun Unban(uid: String) {
        var isLastActionSuccess = true
        var lastException = Exception("Unknown Error!")

        val databaseUser = FirebaseFirestore
            .getInstance()
            .collection(Constants.Collections.USERS)
            .document(uid)
            .get()
            .addOnFailureListener {
                isLastActionSuccess = false
                lastException = it
            }
            .await()

        if (!isLastActionSuccess) {
            listener.onError(lastException)
            return
        }

        if (!databaseUser.exists()) {
            listener.onError(Exception("Użytkownik nie istnieje w bazie danych!"))
            return
        }

        val userToUnban = databaseUser.toObject(HotUser::class.java)

        if (userToUnban != null) {
            userToUnban.isBanned = false
            userToUnban.bannedTo = 0
            userToUnban.banReason = ""
            userToUnban.bannedBy = ""

            FirebaseFirestore
                .getInstance()
                .collection(Constants.Collections.USERS)
                .document(uid)
                .set(userToUnban)
                .addOnSuccessListener {
                    listener.onUnbanned()
                }
                .addOnFailureListener {
                    listener.onError(it)
                }
                .await()
        } else {
            listener.onError(Exception("Cannot convert document into HotUser object!"))
            return
        }
    }
}

interface UserBanListener {
    fun onBanned() {}

    fun onUnbanned() {}

    fun onError(exception: Exception)
}