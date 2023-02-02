package tw.app.hotshots.database.user

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.authentication.model.User
import tw.app.hotshots.encryption.Encrypt

class UserDatabase(
    private val listener: UserDatabaseListener
) {
    private val databaseInstance = FirebaseFirestore.getInstance()
    private var user = UserSingleton.instance?.user!!
    private val database = databaseInstance.collection("users").document(user.uid)

    suspend fun togglePin(on: Boolean) {
        user.isPinEnabled = on
        save()
    }

    suspend fun changePin(otp: String) {
        user.pin = Encrypt.encrypt(otp)
        togglePin(true)
    }

    suspend fun changeDescription(description: String) {
        user.description = description
        save()
    }

    suspend fun changeAvatar(avatar: String) {
        user.avatar = avatar
        save()
    }

    suspend fun saveUser(_user: User) {
        user = _user
        save()
    }

    private suspend fun save() {
        database.set(user)
            .addOnSuccessListener {
                listener.onUpdated(user)
            }
            .addOnFailureListener {
                listener.onError(it)
            }
            .await()
    }
}

interface UserDatabaseListener {
    fun onUpdated(user: User)

    fun onError(exception: java.lang.Exception)
}