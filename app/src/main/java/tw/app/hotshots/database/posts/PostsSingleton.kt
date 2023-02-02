package tw.app.hotshots.database.posts

import tw.app.hotshots.model.main.Post

class PostsSingleton {
    private var postsList: MutableList<Post> = arrayListOf()

    companion object {
        private var postsSingleton: PostsSingleton? = null

        val Instance: PostsSingleton
        get() {
            if (postsSingleton == null) {
                postsSingleton = PostsSingleton()
            }

            return postsSingleton!!
        }
    }

    fun getStoredPosts(): MutableList<Post> {
        return postsList;
    }

    fun storePosts(posts: MutableList<Post>) {
        postsList = posts
    }
}