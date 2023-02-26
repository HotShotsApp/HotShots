package tw.app.hotshots.app

import android.app.Activity
import android.app.Application
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.connection.FileDownloadUrlConnection
import tw.app.hotshots.BuildConfig
import tw.app.hotshots.R
import tw.app.hotshots.activity.MainActivity
import tw.app.hotshots.authentication.model.User
import tw.app.hotshots.database.user.UserWatcher
import tw.app.hotshots.database.user.UserWatcherListener
import tw.app.hotshots.logger.LogType
import tw.app.hotshots.logger.Logger
import tw.app.hotshots.settings.Settings
import tw.app.hotshots.util.TimeUtil
import kotlin.system.exitProcess

class HotShots : Application() {

    private var mCurrentActivity: Activity? = null;

    fun getCurrentActivity(): Activity? {
        return mCurrentActivity
    }

    fun setCurrentActivity(activity: Activity) {
        mCurrentActivity = activity
    }

    override fun onCreate() {
        super.onCreate()

        FileDownloader.setup(this)

        /*FileDownloader.setupOnApplicationOnCreate(this)
            .connectionCreator(FileDownloadUrlConnection
                .Creator(FileDownloadUrlConnection.Configuration()
                    .connectTimeout(15_000)
                    .readTimeout(15_000)
                ))
            .commit()*/

        // Initialize Logger
        Logger.initialize(this)
        Logger.log(
            "App & Logger Initialized at ${TimeUtil.currentTimeToLong()}" +
                    "\n" +
                    "Android SDK: ${Build.VERSION.SDK_INT}\n" +
                    "HotShots App:\n" +
                    "- Build: ${BuildConfig.BUILD_TYPE}\n" +
                    "- Build Version: ${BuildConfig.VERSION_CODE}.${BuildConfig.VERSION_NAME}",
            LogType.INITIALIZATION
        )

        // Initialize Settings
        Settings.initialize(this)

        // Apply Dynamic Colors
        DynamicColors.applyToActivitiesIfAvailable(this)
        Logger.log(
            "Dynamic Colors Available: ${DynamicColors.isDynamicColorAvailable()}",
            LogType.INITIALIZATION
        )

        if (BuildConfig.DEBUG) {
            val debugNotice =
                "<-- Debug Mode -->\nIf something happens, check LogCat instead!"

            Logger.log(
                debugNotice,
                LogType.ERROR // TODO: Should add `NOTICE` LogType
            )
        }

        setupUserWatcher()
    }

    private fun setupUserWatcher() {
        var TAG = "HotShots Application"
        Log.d(TAG, "onCreate: Setting up UserWatcher")
        if (FirebaseAuth.getInstance().currentUser != null) {
            Log.d(TAG, "onCreate: Instance of User is not null, getting user uid")
            var currentUserUid = FirebaseAuth.getInstance().currentUser!!.uid

            UserWatcher.WatchUser(
                currentUserUid,
                object : UserWatcherListener {
                    override fun onUserDataChanged(user: User) {
                        Log.d(TAG, "onUserDataChanged: Triggered")
                        val isMainActivity = getCurrentActivity() is MainActivity

                        if (isMainActivity) {
                            Log.d(TAG, "onUserDataChanged: UserSingleton user variable is not null")
                            Log.d(TAG, "onUserDataChanged: IsBanned: ${user.isBanned}, BannedTo: ${user.bannedTo}")
                            var isBanned = user.isBanned
                            var bannedTo = user.bannedTo
                            var banReason = user.banReason

                            if (isBanned) {
                                if (bannedTo > TimeUtil.currentTimeToLong()) {
                                    var bannedMessage =
                                        "Twoje konto zostało zbanowane!\n\nPowód:\n" +
                                                banReason + "\n\n" + "Zakończy się dnia:\n" +
                                                TimeUtil.convertLongToTime(bannedTo)

                                    if (getCurrentActivity() == null) {
                                        Toast.makeText(applicationContext, bannedMessage, Toast.LENGTH_LONG).show()
                                        exitProcess(0)
                                    } else {
                                        var bannedDialog =
                                            MaterialAlertDialogBuilder(getCurrentActivity()!!)
                                        bannedDialog.setTitle(getString(R.string.banned_dialog_title))
                                        bannedDialog.setMessage(bannedMessage)
                                        bannedDialog.setPositiveButton(getString(R.string.banned_dialog_positive_button)) { _, _ ->
                                            exitProcess(0)
                                        }
                                        bannedDialog.setOnDismissListener {
                                            exitProcess(0)
                                        }
                                        bannedDialog.setCancelable(false)
                                        bannedDialog.show()
                                        Log.d(TAG, "onUserDataChanged: Dialog banned shows")
                                    }
                                }
                            }
                        }
                    }

                    override fun onError(reason: String) {
                        Toast.makeText(applicationContext, "UserWatcher: $reason", Toast.LENGTH_LONG).show()
                    }
                }
            )
            Log.d(TAG, "onCreate: UserWatcher sets up successfully")
        }
    }
}