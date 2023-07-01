package tw.app.hotshots.database.ban

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.Constants

class CheckBanDevice {
    public suspend fun checkIfDeviceIsBanned(deviceId: String, listener: CheckBanDeviceListener) {
        val result = FirebaseFirestore.getInstance()
            .collection(Constants.Collections.BANNED_DEVICES)
            .whereEqualTo("deviceid", deviceId)
            .get()
            .addOnSuccessListener {
                if (it.isEmpty) {
                    listener.onResult(isBanned = false)
                } else {
                    listener.onResult(isBanned = true)
                }
            }
            .addOnFailureListener {
                listener.onError(it)
            }
            .await()
    }
}

interface CheckBanDeviceListener {
    fun onResult(isBanned: Boolean)

    fun onError(exception: Exception)
}