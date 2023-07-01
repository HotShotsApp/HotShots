package tw.app.hotshots.database.ban

import android.bluetooth.BluetoothManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.Constants
import tw.app.hotshots.util.DeviceID
import kotlin.system.exitProcess

class BanDevice {
    public suspend fun invoke(userUid: String, deviceId: String, listener: BanDeviceListener) {
        val hashData = HashMap<String, String>()
        hashData["useruid"] = userUid
        hashData["deviceid"] = deviceId

        FirebaseFirestore
            .getInstance()
            .collection(Constants.Collections.BANNED_DEVICES)
            .document(userUid)
            .set(hashData)
            .addOnSuccessListener {
                listener.onBanned()
            }
            .addOnFailureListener {
                listener.onError(it)
            }
            .await()
    }
}

interface BanDeviceListener {
    fun onBanned()

    fun onError(exception: Exception)
}