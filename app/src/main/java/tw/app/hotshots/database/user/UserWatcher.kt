package tw.app.hotshots.database.user

import com.google.firebase.firestore.FirebaseFirestore
import tw.app.hotshots.authentication.model.User

object UserWatcher {
    fun WatchUser(userUid: String, listener: UserWatcherListener) {
        val userDatabase = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userUid)

        userDatabase.addSnapshotListener { value, error ->
            if (error != null) { // If error it's not null, there is an error lol
                listener.onError(error.message.toString())
            } else { // If there is no errors
                if (value != null) { // And user document is presented
                    UserSingleton.instance?.user = value.toObject(User::class.java)
                    listener.onUserDataChanged(value.toObject(User::class.java) as User)
                } else { // if not presented
                    listener.onError("User Document is null.")
                }
            }
        }
    }
}

interface UserWatcherListener {

    /**
     * User changes listener
     *
     * When some value in [User] in database changes, this will be triggered.
     * You can get changes by getting a Instance of [UserSingleton]
     */
    fun onUserDataChanged(user: User)

    fun onError(reason: String)
}