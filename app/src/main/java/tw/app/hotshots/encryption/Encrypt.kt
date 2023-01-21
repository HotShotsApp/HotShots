package tw.app.hotshots.encryption

import java.security.MessageDigest
import kotlin.experimental.and

object Encrypt {

    /**
     * Encrypt
     * Encrypts given string with SHA-512
     *
     * @return encrypted [String]
     */
    fun encrypt(text: String): String {
        val md: MessageDigest = MessageDigest.getInstance("SHA-512")
        val digest: ByteArray = md.digest(text.toByteArray())
        val sb = StringBuilder()
        for (i in digest.indices) {
            sb.append(((digest[i] and 0xff.toByte()) + 0x100).toString(16).substring(1))
        }
        return sb.toString()
    }
}