package tw.app.hotshots.util

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.ContentResolver
import android.provider.Settings
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class DeviceID {

    /**
     * Since the application is in an early version, currently the bluetooth module address is not encrypted.
     *
     * In the release version, the address will be encrypted with a key that will be taken from the database.
     */
    @SuppressLint("HardwareIds")
    public fun getUniqueDeviceID(contentResolver: ContentResolver): String {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }
}