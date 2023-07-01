package tw.app.hotshots.database.users

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.Constants
import tw.app.hotshots.authentication.model.HotUser
import tw.app.hotshots.logger.Logger
import tw.app.hotshots.singleton.UserSingleton
import java.lang.Exception

class UpdateUser(
    private val user: HotUser,
    private val listener: UpdateUserListener
) {
    suspend fun invoke() {
        val dummy = FirebaseFirestore.getInstance()
            .collection(Constants.Collections.USERS)
            .document(user.uid)
            .set(user)
            .addOnSuccessListener {
                listener.onSuccess(user)
            }
            .addOnFailureListener {
                listener.onError(it)
            }
            .await()
    }
}

interface UpdateUserListener {
    fun onSuccess(user: HotUser) {
        Logger("UpdateUserListener").LogE("onSuccess", "Success - updating user singleton")
        UserSingleton.instance?.user = user
    }

    fun onError(exception: Exception) {
        var exceptionDetails: String? = if (exception.message != null) {
            exception.message.toString()
        } else {
            "Unknown error occurred!"
        }

        Logger("UpdateUserListener").LogE("onError", exceptionDetails!!)
        exceptionDetails = null
    }
}