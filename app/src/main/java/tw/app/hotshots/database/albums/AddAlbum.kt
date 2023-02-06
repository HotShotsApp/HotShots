package tw.app.hotshots.database.albums

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.model.main.Album
import tw.app.hotshots.util.ConstantsDatabaseCollections

class AddAlbum {
    suspend fun invoke(influencerUid: String, album: Album, listener: OnAddAlbumListener) {
        val database = FirebaseFirestore.getInstance().collection(ConstantsDatabaseCollections.INFLUENCERS).document(influencerUid).collection(ConstantsDatabaseCollections.ALBUMS).document(album.uid)

        database
            .set(album)
            .addOnFailureListener {
                listener.onError(it.message.toString())
                return@addOnFailureListener
            }
            .await()

        for (image in album.images) {
            database
                .collection(ConstantsDatabaseCollections.IMAGES)
                .document(image.uid)
                .set(image)
                .await()
        }

        listener.onAdded()
    }
}

interface OnAddAlbumListener {
    fun onAdded()

    fun onError(reason: String)
}