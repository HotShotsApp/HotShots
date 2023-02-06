package tw.app.hotshots.database.influencers

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.model.main.Influencer

class AddInfluencer {
    suspend fun invoke(influencer: Influencer, listener: AddInfluencerListener) {
        AddInfluencerToDatabase(influencer, listener)
    }

    suspend fun AddInfluencerToDatabase(influencer: Influencer, listener: AddInfluencerListener) {
        val database = FirebaseFirestore.getInstance().collection("influencers")

        if (InfluencersSingleton.Instance.getStoredInfluencers().isNotEmpty()) {
            for (influencerExist in InfluencersSingleton.Instance.getStoredInfluencers()) {
                if (influencer.nickname == influencerExist.nickname) {
                    listener.onError("Osoba o takim pseudonimie już istnieje!")
                    return
                }
            }
        }

        database
            .document(influencer.uid)
            .set(influencer)
            .addOnSuccessListener {
                listener.onAdded()
            }
            .addOnFailureListener {
                listener.onError(it.message.toString())
            }
            .await()
    }
}

interface AddInfluencerListener {
    fun onAdded()

    fun onError(reason: String)
}