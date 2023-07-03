package tw.app.hotshots.authentication

import android.bluetooth.BluetoothManager
import android.content.ContentResolver
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.authentication.model.HotUser
import tw.app.hotshots.database.users.AddUser
import tw.app.hotshots.database.users.AddUserListener
import tw.app.hotshots.database.users.GetUser
import tw.app.hotshots.database.users.GetUserListener
import tw.app.hotshots.logger.Logger
import tw.app.hotshots.singleton.UserSingleton
import tw.app.hotshots.util.DeviceID
import tw.app.hotshots.util.TimeUtil
import java.lang.Exception

class Authentication(
    private val listener: AuthenticationListener?
) {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    public suspend fun splashCheck(): Authentication {
        if (firebaseAuth.currentUser != null) {
            GetUser(object : GetUserListener {
                override fun onSuccess(user: HotUser) {
                    listener?.onAlreadyAuthenticated(user)
                }

                override fun onError(exception: Exception) {
                    listener?.onError(exception)
                }
            }).GetByUID(firebaseAuth.uid!!)
        } else if (firebaseAuth.currentUser == null) {
            listener?.onAuthenticationNeeded()
        }

        return this
    }

    public suspend fun login(email: String, password: String) {
        var wasLastActionSuccessful = true
        var uid = ""
        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                uid = it.user!!.uid
            }
            .addOnFailureListener {
                wasLastActionSuccessful = false
                listener?.onError(it)
            }
            .await()

        if (wasLastActionSuccessful && uid.isNotBlank()) {
            GetUser(object : GetUserListener {
                override fun onSuccess(user: HotUser) {
                    listener?.onLogin(user)
                }

                override fun onError(exception: Exception) {
                    listener?.onError(exception)
                }
            }).GetByUID(uid)
        }
    }

    public suspend fun register(email: String, username: String, password: String, contentResolver: ContentResolver) {
        var wasLastActionSuccessful = true
        var hotUser = HotUser(
            uid = "",
            name = username,
            email = email,
            createdAt = TimeUtil.currentTimeToLong(),
            deviceID = DeviceID().getUniqueDeviceID(contentResolver)
        )

        val dummy = FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Logger("Authentication").LogI("OnSuccessListener", "Success... Setting user uid!")
                hotUser.uid = it.user!!.uid
            }
            .addOnFailureListener {
                wasLastActionSuccessful = false
                listener?.onError(it)
            }
            .await()

        if (wasLastActionSuccessful) {
            AddUser(hotUser, object : AddUserListener {
                override fun onSuccess() {
                    super.onSuccess()

                    listener?.onRegistered(hotUser)
                }

                override fun onError(exception: Exception) {
                    super.onError(exception)
                    firebaseAuth.currentUser!!.delete()

                    listener?.onError(exception)
                }
            }).invoke()
        } else {
            firebaseAuth.currentUser!!.delete()
            listener?.onError(Exception("Unknown Error! WasLastActionSuccessful: $wasLastActionSuccessful IsUserNull: ${hotUser == null} Line: 101"))
        }
    }

    public fun logout() {
        firebaseAuth.signOut()
    }
}

interface AuthenticationListener {
    fun onAlreadyAuthenticated(user: HotUser) {
        Logger("AuthenticationListener").LogI("onAlreadyAuthenticated", "Setting UserSingleton...")
        UserSingleton.instance?.user = user
    }

    fun onAuthenticationNeeded() {
        Logger("AuthenticationListener").LogI("onAuthenticationNeeded", "Authentication Needed...")
    }

    fun onRegistered(user: HotUser) {
        Logger("AuthenticationListener").LogI("onRegistered", "Setting UserSingleton...")
        UserSingleton.instance?.user = user
    }

    fun onLogin(user: HotUser) {
        Logger("AuthenticationListener").LogI("onLogin", "Setting UserSingleton...")
        UserSingleton.instance?.user = user
    }

    fun onError(exception: Exception) {
        var exceptionDetails: String? = if (exception.message != null) {
            exception.message.toString()
        } else {
            "Unknown error occurred!"
        }

        Logger("AuthenticationListener").LogE("onError", exceptionDetails!!)
        exceptionDetails = null
    }
}