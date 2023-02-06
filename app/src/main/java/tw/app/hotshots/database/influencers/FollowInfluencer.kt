package tw.app.hotshots.database.influencers

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.database.user.UserSingleton
import tw.app.hotshots.model.main.Follow
import tw.app.hotshots.util.ConstantsDatabaseCollections

class FollowInfluencer {
    suspend fun invoke(influencerUid: String, settings: FollowSettings, onFollowListener: OnFollowListener) {
        if (settings == FollowSettings.FOLLOW) {
            follow(influencerUid, onFollowListener)
        } else if (settings == FollowSettings.UNFOLLOW) {
            unfollow(influencerUid, onFollowListener)
        }
    }

    private suspend fun follow(influencerUid: String, onFollowListener: OnFollowListener) {
        val follow = Follow(UserSingleton.instance?.user?.uid!!)

        val database = FirebaseFirestore.getInstance()
            .collection(ConstantsDatabaseCollections.INFLUENCERS)
            .document(influencerUid)
            .collection(ConstantsDatabaseCollections.FOLLOWS)
            .document(follow.followedBy)
            .set(follow)
            .addOnFailureListener {
                onFollowListener.onError(it.message.toString())
            }
            .await()

        onFollowListener.onFollow()
    }

    private suspend fun unfollow(influencerUid: String, onFollowListener: OnFollowListener) {
        val database = FirebaseFirestore.getInstance()
            .collection(ConstantsDatabaseCollections.INFLUENCERS)
            .document(influencerUid)
            .collection(ConstantsDatabaseCollections.FOLLOWS)
            .document(UserSingleton.instance?.user?.uid!!)
            .delete()
            .addOnFailureListener {
                onFollowListener.onError(it.message.toString())
            }
            .await()

        onFollowListener.onUnFollow()
    }
}

interface OnFollowListener {
    fun onFollow() {}

    fun onUnFollow() {}

    fun onError(reason: String)
}

enum class FollowSettings {
    FOLLOW,
    UNFOLLOW
}