package tw.app.hotshots.database.nicknames

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.Constants

class NickNames {
    public suspend fun checkIfNicknameAvailable(nickname: String, listener: NickCheckListener) {
        val result = FirebaseFirestore
            .getInstance()
            .collection(Constants.Collections.NICKNAMES)
            .whereEqualTo("nickname", nickname)
            .get()
            .addOnSuccessListener {
                if (it.isEmpty) {
                    listener.onAvailable()
                } else {
                    listener.onTaken()
                }
            }
            .addOnFailureListener {
                listener.onError(it)
            }
            .await()
    }
}

interface NickCheckListener {
    fun onAvailable()

    fun onTaken()

    fun onError(exception: Exception)
}