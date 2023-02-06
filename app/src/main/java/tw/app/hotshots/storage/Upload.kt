package tw.app.hotshots.storage

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import tw.app.hotshots.util.UidGenerator
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream


class Upload {
    suspend fun Bitmap(
        database: String,
        bitmapImage: Bitmap,
        listener: UploadListener
    ) {
        val baos = ByteArrayOutputStream()
        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val data: ByteArray = baos.toByteArray()

        val storage = FirebaseStorage.getInstance("gs://walkboner-72c59.appspot.com")
        val storageRef = storage.reference
        val mountainsRef = storageRef.child("$database/" + UidGenerator.Generate(12) + ".png")

        val uploadTask: UploadTask = mountainsRef.putBytes(data)
        uploadTask.addOnFailureListener {
            listener.onError(it.message.toString())
        }
            .addOnSuccessListener { taskSnapshot ->
                mountainsRef.downloadUrl.addOnSuccessListener {
                    listener.onUploaded(it.toString())
                }
            }.await()
    }

    suspend fun Image(
        storagePath: String,
        imagePaths: MutableList<String>,
        context: Context,
        listener: UploadListener
    ) {
        Log.d("Upload", "Image: Uploading images =>" + imagePaths)

        var progressInList = 1
        for (imagePath in imagePaths) {
            val uid = UidGenerator.Generate()

            var stream: InputStream? = null
            var file = File(imagePath)

            Log.d("Upload", "Image: Uploading image =>" + imagePath)

            try {
                stream = withContext(Dispatchers.IO) {
                    FileInputStream(file)
                }
            } catch (ex: Exception) {
                listener.onError(ex.message.toString())
            }

            if (stream == null) {
                listener.onError("Stream jest pusty!")
                return
            }

            val fileExtension: String =
                imagePath.toString().substring(imagePath.toString().lastIndexOf("."))

            val storage = FirebaseStorage.getInstance("gs://walkboner-72c59.appspot.com")
            val storageRef = storage.reference
            val mountainsRef = storageRef.child(storagePath + uid + fileExtension)

            val uploadTask = mountainsRef.putStream(stream)
            uploadTask.addOnFailureListener { exception: java.lang.Exception ->
                listener.onError(exception.message.toString())
            }
                .addOnSuccessListener {
                    mountainsRef.downloadUrl
                        .addOnSuccessListener { uri: Uri ->
                            listener.onUploaded(
                                uri.toString()
                            )
                            if (progressInList != imagePaths.size) {
                                progressInList++
                                listener.onFileChanged(progressInList, imagePaths.size)
                            } else {
                                listener.onFinished()
                            }
                        }
                }.addOnProgressListener { snapshot ->
                    val progress = 100.0 * snapshot.bytesTransferred / snapshot.totalByteCount
                    listener.progress(progress.toInt())
                }.await()
        }
    }
}

interface UploadListener {
    fun onUploaded(fileUrl: String) {}

    fun onFinished() {}

    fun progress(progress: Int) {}

    fun onFileChanged(currentPosition: Int, size: Int) {}

    fun onError(reason: String) {}
}