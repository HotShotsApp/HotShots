package tw.app.hotshots.database.albums

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.model.main.Album
import tw.app.hotshots.model.main.Like
import tw.app.hotshots.model.media.Image
import tw.app.hotshots.util.ConstantsDatabaseCollections

class GetAlbums {
    suspend fun invoke(uid: String, listener: OnGetAlbumsListener) {
        getVisibleAlbums(uid, listener)
    }

    suspend fun invoke(influencerUid: String, albumUid: String, listener: OnGetAlbumListener) {
        getAlbum(influencerUid, albumUid, listener)
    }

    private suspend fun getAlbum(influencerUid: String, albumUid: String, listener: OnGetAlbumListener) {
        val database = FirebaseFirestore.getInstance().collection(ConstantsDatabaseCollections.INFLUENCERS).document(influencerUid).collection(ConstantsDatabaseCollections.ALBUMS).document(albumUid)
        val albumDocument = database.get().await()

        if (albumDocument.exists()) {
                val album = albumDocument.toObject(Album::class.java)!!

                val albumLikes: MutableList<Like> = arrayListOf()
                val albumImages: MutableList<Image> = arrayListOf()

                // Get likes
                val likesDatabase =
                    database.collection("likes").get().await()

                for (likeDocument in likesDatabase.documents) {
                    val like = likeDocument.toObject(Like::class.java)

                    if (like != null)
                        albumLikes.add(like)
                }

                // Get images
                val imagesDatabase =
                    database.collection("images").get().await()

                for (imageDocument in imagesDatabase.documents) {
                    val image = imageDocument.toObject(Image::class.java)

                    if (image != null)
                        albumImages.add(image)
                }

                album.images = albumImages
                album.likes = albumLikes

            listener.onReceived(album)
        } else {
            listener.onError("Nie znaleziono albumu")
        }
    }

    private suspend fun getVisibleAlbums(uid: String, listener: OnGetAlbumsListener) {
        val database = FirebaseFirestore.getInstance().collection(ConstantsDatabaseCollections.INFLUENCERS).document(uid).collection(ConstantsDatabaseCollections.ALBUMS)
        val albumsDatabase = database.get().await()

        val albums: MutableList<Album> = arrayListOf()

        if (!albumsDatabase.isEmpty) {
            for (albumDocument in albumsDatabase.documents) {
                val album = albumDocument.toObject(Album::class.java)!!

                val albumLikes: MutableList<Like> = arrayListOf()
                val albumImages: MutableList<Image> = arrayListOf()

                // Get likes
                val likesDatabase =
                    database.document(albumDocument.id).collection(ConstantsDatabaseCollections.LIKES).get().await()

                for (likeDocument in likesDatabase.documents) {
                    val like = likeDocument.toObject(Like::class.java)

                    if (like != null)
                        albumLikes.add(like)
                }

                // Get images
                val imagesDatabase =
                    database.document(albumDocument.id).collection(ConstantsDatabaseCollections.IMAGES).get().await()

                for (imageDocument in imagesDatabase.documents) {
                    val image = imageDocument.toObject(Image::class.java)

                    if (image != null)
                        albumImages.add(image)
                }

                album.images = albumImages
                album.likes = albumLikes

                albums.add(album)
            }

            listener.onReceived(albums)
        } else {
            listener.onError("Brak albumów")
        }
    }
}

interface OnGetAlbumListener {
    fun onReceived(album: Album)

    fun onError(reason: String)
}

interface OnGetAlbumsListener {
    fun onReceived(albums: MutableList<Album>)

    fun onError(reason: String)
}