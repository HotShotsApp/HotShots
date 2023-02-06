package tw.app.hotshots.database.influencers

import tw.app.hotshots.database.posts.PostsSingleton
import tw.app.hotshots.model.main.Influencer

class InfluencersSingleton {
    private var influencersList: MutableList<Influencer> = arrayListOf()

    companion object {
        private var influencersSingleton: InfluencersSingleton? = null

        val Instance: InfluencersSingleton
            get() {
                if (influencersSingleton == null) {
                    influencersSingleton = InfluencersSingleton()
                }

                return influencersSingleton!!
            }
    }

    fun getStoredInfluencers(): MutableList<Influencer> {
        return influencersList
    }

    fun storeInfluencers(list: MutableList<Influencer>) {
        influencersList = list
    }
}