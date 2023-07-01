package tw.app.hotshots.database.users

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.Constants
import tw.app.hotshots.authentication.model.HotUser
import tw.app.hotshots.logger.Logger
import kotlin.Exception

class AddUser(
    private val user: HotUser,
    private val listener: AddUserListener
) {
    public suspend fun invoke() {
        var isLastActionSuccess = true
        var lastException = Exception("Unknown Error!")

        FirebaseFirestore.getInstance()
            .collection(Constants.Collections.USERS)
            .document(user.uid)
            .set(user)
            .addOnFailureListener {
                isLastActionSuccess = false
                lastException = it
            }
            .await()

        if (!isLastActionSuccess) {
            listener.onError(lastException)
            return
        }

        val nickname = HashMap<String, String>()
        nickname["nickname"] = user.name
        nickname["uid"] = user.uid
        FirebaseFirestore.getInstance()
            .collection(Constants.Collections.NICKNAMES)
            .document(user.uid)
            .set(nickname)
            .addOnSuccessListener {
                listener.onSuccess()
            }
            .addOnFailureListener {
                listener.onError(it)
            }
            .await()
    }
}

interface AddUserListener {
    fun onSuccess() {
        Logger("AddUserListener").LogI("onSuccess", "User added successfully!")
    }

    fun onError(exception: Exception) {
        var exceptionDetails: String? = if (exception.message != null) {
            exception.message.toString()
        } else {
            "Unknown error occurred!"
        }

        Logger("AddUserListener").LogE("onError", exceptionDetails!!)
        exceptionDetails = null
    }
}