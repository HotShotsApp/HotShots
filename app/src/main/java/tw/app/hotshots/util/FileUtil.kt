package tw.app.hotshots.util

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object FileUtil {
    fun saveBitmapAsFile(context: Context, imageBitmap: Bitmap): File {
        val wrapper = ContextWrapper(context)

        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
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
        var target: File? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            target = File(
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0.toLong())
                ).applicationInfo.dataDir.toString()
            )
        } else {
            target = File(
                packageManager.getPackageInfo(
                    packageName,
                    0
                ).applicationInfo.dataDir.toString()
            )
        }

        target = origin.copyTo(target, true)

        return target
    }

    fun checkIfFileExist(path: String): Boolean {
        val file = File(path)
        return file.exists()
    }
}