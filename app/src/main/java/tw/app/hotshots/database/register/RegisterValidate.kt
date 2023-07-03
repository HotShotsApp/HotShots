package tw.app.hotshots.database.register

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.Constants
import tw.app.hotshots.logger.Logger

class RegisterValidate {
    public suspend fun Validate(nickname: String, deviceId: String, listener: RegisterValidateListener) {
        /**
         * First check if device is banned
         */

        var lastActionSuccess = true
        var lastException = Exception("Unknown Error!")
        var isDeviceBanned = false

        Logger("RegisterValidate").LogI("checkIfDeviceIsBanned", "Checking...")

        val banResult = FirebaseFirestore.getInstance()
            .collection(Constants.Collections.BANNED_DEVICES)
            .whereEqualTo("deviceid", deviceId)
            .get()
            .addOnFailureListener {
                Logger("RegisterValidate").LogI("checkIfDeviceIsBanned", "Failed...")
                lastActionSuccess = false
                lastException = it
            }
            .await()

        if (!lastActionSuccess) {
            Logger("RegisterValidate").LogI("checkIfDeviceIsBanned", "Failed...")
            listener.onError(lastException)
            return
        }

        if (banResult.isEmpty) {
            isDeviceBanned = false
            Logger("RegisterValidate").LogI("checkIfDeviceIsBanned", "Notifying that device is not banned...")
        } else {
            isDeviceBanned = true
            Logger("RegisterValidate").LogI("checkIfDeviceIsBanned", "Notifying that device is banned...")
        }

        /**
         * Next check is if the username is available
         */
        var isNickAvailable = true
        var isNickCheckSuccess = true
        var lastNickException = Exception("Unknown Error!")
        val nickResult = FirebaseFirestore
            .getInstance()
            .collection(Constants.Collections.NICKNAMES)
            .whereEqualTo("nickname", nickname)
            .get()
            .addOnFailureListener {
                isNickCheckSuccess = false
                lastNickException = it
            }
            .await()

        if (!isNickCheckSuccess) {
            listener.onError(lastNickException)
            return
        }

        isNickAvailable = nickResult.isEmpty

        if (isDeviceBanned) {
            listener.onFailed(VALIDATION_TYPE_FAIL.DEVICE_BANNED)
            return
        }

        if (!isNickAvailable) {
            listener.onFailed(VALIDATION_TYPE_FAIL.USERNAME_NOT_AVAILABLE)
            return
        }

        listener.onSuccess()
    }
}

enum class VALIDATION_TYPE_FAIL {
    USERNAME_NOT_AVAILABLE,
    DEVICE_BANNED
}

interface RegisterValidateListener {
    fun onSuccess()

    fun onFailed(reason: VALIDATION_TYPE_FAIL)

    fun onError(exception: Exception)
}