package tw.app.hotshots.app.database

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.BuildConfig
import tw.app.hotshots.Constants
import tw.app.hotshots.app.database.models.Version

class CheckVersion {
    public suspend fun invoke(listener: CheckVersionListener) {
        var isLastActionSuccess = true
        var lastException = Exception("Unknown Error!")

        val document = FirebaseFirestore
            .getInstance()
            .collection(Constants.Collections.APP)
            .document(Constants.Documents.VERSION)
            .get()
            .addOnFailureListener {
                isLastActionSuccess = false
                lastException = it
            }
            .await()

        if (!isLastActionSuccess) {
            listener.onError(lastException)
            return
        }

        if (!document.exists()) {
            listener.onError(Exception("Document doesn't exist!"))
            return
        }

        val versionObject = document.toObject(Version::class.java)

        if (versionObject == null) {
            listener.onError(Exception("Cannot convert document into Version object!"))
            return
        }

        val isNewVersionAvailable = BuildConfig.VERSION_NAME != versionObject.version
        if (isNewVersionAvailable)
            listener.onChecked(true, versionObject)
        else
            listener.onChecked(false, null)
    }
}

interface CheckVersionListener {
    fun onChecked(isUpdateAvailable: Boolean, newVersion: Version?)

    fun onError(exception: Exception)
}