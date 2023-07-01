package tw.app.hotshots.singleton

import tw.app.hotshots.authentication.model.HotUser

class UserSingleton

private constructor() {
    var user: HotUser? = null

    companion object {
        private var userSingleton: UserSingleton? = null

        val instance: UserSingleton?
            get() {
                if (userSingleton == null) {
                    userSingleton = UserSingleton()
                }

                return userSingleton
            }
    }
}