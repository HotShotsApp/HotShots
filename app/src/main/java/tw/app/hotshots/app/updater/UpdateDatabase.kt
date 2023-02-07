package tw.app.hotshots.app.updater

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.BuildConfig
import tw.app.hotshots.app.model.Version
import tw.app.hotshots.util.ConstantsDatabaseCollections
import tw.app.hotshots.util.ConstantsDatabaseDocuments

class UpdateDatabase {
    suspend fun checkIfUpdateAvailable(listener: UpdateAvailableChecker) {
        if (BuildConfig.DEBUG) {
            listener.onFailed("Updates is not available in debug mode!")
            return
        }

        val versionSnapshot = FirebaseFirestore.getInstance()
            .collection(ConstantsDatabaseCollections.APP)
            .document(ConstantsDatabaseDocuments.VERSION)
            .get()
            .addOnFailureListener {
                listener.onFailed(it.message.toString())
            }
            .await()

        if (versionSnapshot.exists()) {
            val version = versionSnapshot.toObject(Version::class.java)

            if (version != null && version.versionName != BuildConfig.VERSION_NAME) {
                listener.onAvailable(version)
            } else {
                listener.onFailed("Version value is empty!")
            }
        } else {
            listener.onFailed("VersionSnapshot do not exist!")
        }
    }
}

interface UpdateAvailableChecker {
    fun onAvailable(newVersion: Version)

    fun onFailed(reason: String)
}