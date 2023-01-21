package tw.app.hotshots.database.posts.user

import tw.app.hotshots.authentication.model.User


class UserSingleton
/**
 * The private constructor for the Customer List Singleton class
 */
private constructor() {
    var user: User? = null

    companion object {
        private var mCustLab: UserSingleton? = null

        //instantiate a new CustomerLab if we didn't instantiate one yet
        val instance: UserSingleton?
            get() {
                //instantiate a new CustomerLab if we didn't instantiate one yet
                if (mCustLab == null) {
                    mCustLab = UserSingleton()
                }
                return mCustLab
            }
    }
}