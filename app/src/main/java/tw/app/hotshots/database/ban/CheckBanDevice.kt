package tw.app.hotshots.database.ban

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.Constants
import tw.app.hotshots.logger.Logger

class CheckBanDevice {
    public suspend fun checkIfDeviceIsBanned(deviceId: String, listener: CheckBanDeviceListener) {
        var lastActionSuccess = true
        var lastException = Exception("Unknown Error!")

        Logger("AuthFragment").LogI("checkIfDeviceIsBanned", "Checking...")

        val result = FirebaseFirestore.getInstance()
            .collection(Constants.Collections.BANNED_DEVICES)
            .whereEqualTo("deviceid", deviceId)
            .get()
            .addOnFailureListener {
                Logger("AuthFragment").LogI("checkIfDeviceIsBanned", "Failed...")
                lastActionSuccess = false
                lastException = it
            }
            .await()

        if (!lastActionSuccess) {
            Logger("AuthFragment").LogI("checkIfDeviceIsBanned", "Failed...")
            listener.onError(lastException)
            return
        }

        if (result.isEmpty) {
            listener.onResult(isBanned = false)
            Logger("AuthFragment").LogI("checkIfDeviceIsBanned", "Notifying that device is not banned...")
        } else {
            listener.onResult(isBanned = true)
            Logger("AuthFragment").LogI("checkIfDeviceIsBanned", "Notifying that device is banned...")
        }
    }
}

interface CheckBanDeviceListener {
    fun onResult(isBanned: Boolean)

    fun onError(exception: Exception)
}