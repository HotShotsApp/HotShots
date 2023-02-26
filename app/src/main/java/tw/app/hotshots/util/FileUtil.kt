package tw.app.hotshots.util

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Environment
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream

object FileUtil {
    fun saveBitmapAsFile(context: Context, imageBitmap: Bitmap): File {
        val wrapper = ContextWrapper(context)

        var file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), ".links/images")

        if (!file.exists())
            if (!file.mkdirs())
                throw Exception("Nie można utworzyć folderu dla zdjęć linków! Sprawdź czy aplikacja posiada uprawnienia do czytania i zapisu w pamięci!")

        file = File(file, "${UidGenerator.Generate()}.jpg")

        val stream: OutputStream = FileOutputStream(file)
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 25, stream)
        stream.flush()
        stream.close()

        return file
    }

    fun copyFileToAppFolder(
        packageManager: PackageManager,
        packageName: String,
        pathToOrigin: String
    ): File {
        val origin = File(pathToOrigin)
        var target = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), ".links/images")

        if (!target.exists())
            if (!target.mkdir())
                throw Exception("Nie można utworzyć folderu dla zdjęć linków! Sprawdź czy aplikacja posiada uprawnienia do czytania i zapisu w pamięci!")

        target = origin.copyTo(target, true)

        return target
    }

    fun checkIfFileExist(file: File): Boolean {
        return file.exists()
    }

    fun isLinkBackupAvailable(context: Context): Boolean {
        val path: File = context.filesDir
        val file = File(path, "links.hsb")

        return file.exists()
    }

    fun removeLinkBackupFile(context: Context) {
        val path: File = context.filesDir
        val file = File(path, "links.hsb")

        if (file.exists())
            file.delete()
    }

    fun writeLinkBackupToFile(text: String, context: Context, listener: OnWriteToFileListener) {
        val path: File = context.filesDir
        val file = File(path, "links.hsb")
        val stream = FileOutputStream(file)

        try {
            stream.write(text.toByteArray())
            listener.onSuccess()
        } catch (exception: Exception) {
            listener.onError(exception.message.toString())
        }
    }

    fun readLinkBackupFromFile(context: Context, listener: OnReadFromFileListener) {
        val path: File = context.filesDir
        val file = File(path, "links.hsb")

        val length = file.length().toInt()
        val bytes = ByteArray(length)

        val fileInputStream = FileInputStream(file)

        try {
            fileInputStream.read(bytes)
        } catch (exception: Exception) {
            listener.onError(exception.message.toString())
        } finally {
            fileInputStream.close()
        }

        listener.onSuccess(String(bytes))
    }

    interface OnWriteToFileListener {
        fun onSuccess()

        fun onError(reason: String)
    }

    interface OnReadFromFileListener {
        fun onSuccess(content: String)

        fun onError(reason: String)
    }
}