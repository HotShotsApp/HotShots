package tw.app.hotshots.storage

import android.content.Context
import android.net.Uri
import android.util.Log
import tw.app.hotshots.R
import tw.app.hotshots.util.UriUtil
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import tw.app.hotshots.util.UidGenerator
import java.io.File
import java.io.FileInputStream
import java.io.InputStream


class Upload {
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

            val fileExtension: String = imagePath.toString().substring(imagePath.toString().lastIndexOf("."))

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
    fun onUploaded(fileUrl: String)

    fun onFinished()

    fun progress(progress: Int)

    fun onFileChanged(currentPosition: Int, size: Int)

    fun onError(reason: String)
}