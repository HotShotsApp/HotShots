package tw.app.hotshots.authentication

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.authentication.model.User
import tw.app.hotshots.database.posts.user.UserSingleton
import tw.app.hotshots.logger.LogType
import tw.app.hotshots.logger.Logger

/**
 * Class to Authenticate user
 *
 * To get current [User] data class, use [UserSingleton.instance]
 */
class Authentication(
    private val listener: AuthenticationListener?
) {
    private val TAG = "Authentication"

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var database = FirebaseFirestore.getInstance().collection("users")

    suspend fun initialize(): Authentication {
        logNormal("$TAG Initialize | Start")

        if (auth.currentUser != null) {
            logNormal("$TAG Initialize | User is already logged in...\nGetting his data")
            database.document(auth.currentUser!!.uid).get()
                .addOnSuccessListener {
                    val user = it.toObject(User::class.java)!!
                    logNormal("$TAG Database | User Finded & his data parsed!\nNotify App")
                    listener?.onUserAlreadyLogged(user)
                }
                .addOnFailureListener {
                    logError("$TAG Database | Error: ${it.message}")
                    listener?.onError(it)
                }.await()
        } else {
            logNormal("$TAG Initialize | User needs to be logged!")
            listener?.onUserAuthenticationNeeded()
        }

        return this
    }

    suspend fun login(email: String, password: String) {
        var authResult: AuthResult? = null
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResultL ->
                if (authResultL.user != null) {
                    authResult = authResultL
                    return@addOnSuccessListener
                } else {
                    listener?.onError(Exception("Unexpected Error!"))
                }
            }
            .addOnFailureListener {
                listener?.onError(it)
            }.await()

        database.document(authResult?.user?.uid!!).get()
            .addOnSuccessListener {
                val user = it.toObject(User::class.java)!!
                listener?.onUserLogin(user)
            }
            .addOnFailureListener {
                listener?.onError(it)
            }.await()
    }

    suspend fun register(email: String, username: String, password: String) {
        logNormal("$TAG Register | Registering $username")

        val authResult = auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                logNormal("$TAG Register | Successfully created $username")
            }
            .addOnFailureListener {
                listener?.onError(it)
            }.await()

        val user = User(
            uid = authResult?.user?.uid!!,
            name = username,
            email = email
        )

        logNormal("$TAG Register | Adding $username to database")
        database
            .document(authResult.user?.uid!!)
            .set(user)
            .addOnSuccessListener {
                listener?.onUserRegister(user)
            }
            .addOnFailureListener {
                authResult.user?.delete()
                listener?.onError(it)
            }.await()
    }

    private fun logNormal(message: String) {
        Logger.log(
            message,
            LogType.NORMAL
        )
    }

    private fun logError(message: String) {
        Logger.log(
            message,
            LogType.ERROR
        )
    }
}

interface AuthenticationListener {
    fun onUserAlreadyLogged(user: User) {
        UserSingleton.instance?.user = user
    }

    fun onUserAuthenticationNeeded() {}

    fun onUserLogin(user: User) {
        UserSingleton.instance?.user = user

        Logger.log(
            "Authentication | User ${user.name} successfully logged!",
            LogType.NORMAL
        )
    }

    fun onUserRegister(user: User) {
        UserSingleton.instance?.user = user

        Logger.log(
            "Authentication | User ${user.name} successfully created!",
            LogType.NORMAL
        )
    }

    fun onError(e: Exception) {
        Logger.log(
            "Authentication | Error!\n\nReason:\n${e.message}",
            LogType.CRITICAL
        )
    }
}