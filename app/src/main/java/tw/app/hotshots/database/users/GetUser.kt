package tw.app.hotshots.database.users

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.Constants
import tw.app.hotshots.authentication.model.HotUser
import java.lang.Exception

class GetUser(
    private val listener: GetUserListener
) {
    public suspend fun GetByUID(uid: String) {
        var wasLastActionSuccessful: Boolean = true;

        val userDocument = FirebaseFirestore.getInstance()
            .collection(Constants.Collections.USERS)
            .document(uid)
            .get()
            .addOnFailureListener {
                listener.onError(it)
                wasLastActionSuccessful = false
            }
            .await()

        if (!wasLastActionSuccessful)
            return

        if (!userDocument.exists()) {
            listener.onError(Exception("User document doesn't exist in database!"))
            return
        }

        val userObject = userDocument.toObject(HotUser::class.java)

        if (userObject == null) {
            listener.onError(Exception("Cannot convert DocumentSnapshot to User object!"))
            return
        }

        listener.onSuccess(userObject)
    }
}

interface GetUserListener {
    fun onSuccess(user: HotUser)

    fun onError(exception: Exception)
}