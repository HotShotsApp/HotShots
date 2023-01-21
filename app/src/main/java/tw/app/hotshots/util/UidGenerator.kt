package tw.app.hotshots.util

import java.util.*

object UidGenerator {
    private const val ALLOWED_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm"
    fun Generate(): String {
        val random = Random()
        val sb = StringBuilder(16)
        for (i in 0..15) sb.append(ALLOWED_CHARACTERS[random.nextInt(ALLOWED_CHARACTERS.length)])
        return sb.toString()
    }

    fun Generate(length: Int): String {
        val random = Random()
        val sb = StringBuilder(length)
        for (i in 0 until length) sb.append(ALLOWED_CHARACTERS[random.nextInt(ALLOWED_CHARACTERS.length)])
        return sb.toString()
    }
}