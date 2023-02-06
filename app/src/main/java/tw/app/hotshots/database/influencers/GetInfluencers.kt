package tw.app.hotshots.database.influencers

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.model.main.Follow
import tw.app.hotshots.model.main.Influencer

class GetInfluencers {

    suspend fun invoke(listener: InfluencersListener) {
        getVisibleInfluencers(listener)
    }

    suspend fun byUid(uid: String, listener: SingleInfluencerListener) {
        val influencerSnapshot = FirebaseFirestore.getInstance().collection("influencers").document(uid).get().await()

        if (influencerSnapshot.exists()) {
            val influencer = influencerSnapshot.toObject(Influencer::class.java)!!

            val followsList: MutableList<Follow> = arrayListOf()

            val followsQuerySnapshot = FirebaseFirestore.getInstance().collection("influencers").document(uid).collection("follows").get()
                .addOnFailureListener {
                    listener.onError(it.message.toString())
                    return@addOnFailureListener
                }.await()

            if (followsQuerySnapshot != null) {
                for (followDocument in followsQuerySnapshot.documents) {
                    val follow = followDocument.toObject(Follow::class.java)

                    if (follow != null)
                        followsList.add(follow)
                }

                influencer.follows = followsList
            } else {
                listener.onError("Snapshot is null!")
            }

            listener.onReceived(influencer)
        }
    }

    private suspend fun getVisibleInfluencers(listener: InfluencersListener) {
        val database = FirebaseFirestore.getInstance().collection("influencers")
        var influencersList: MutableList<Influencer> = arrayListOf()

        val querySnapshot = database.get().await()

        if (querySnapshot != null) {
            for (document in querySnapshot.documents) {
                val influencer = document.toObject(Influencer::class.java)!!

                val followsList: MutableList<Follow> = arrayListOf()

                val followsQuerySnapshot = database.document(influencer.uid).collection("follows").get().await()

                if (followsQuerySnapshot != null) {
                    for (followDocument in followsQuerySnapshot.documents) {
                        val follow = followDocument.toObject(Follow::class.java)

                        if (follow != null)
                            followsList.add(follow)
                    }

                    influencer.follows = followsList
                }

                if (!influencer.isHidden)
                    influencersList.add(influencer)
            }
        } else {
            listener.onError("QuerySnapshot is empty!")
            return
        }

        influencersList.sortBy { influencerSort: Influencer -> influencerSort.follows.size }
        influencersList.reverse()

        InfluencersSingleton.Instance.storeInfluencers(influencersList)
        listener.onReceived(influencersList)
    }
}

interface InfluencersListener {
    fun onReceived(influencers: MutableList<Influencer>)

    fun onError(reason: String)
}

interface SingleInfluencerListener {
    fun onReceived(influencer: Influencer)

    fun onError(reason: String)
}