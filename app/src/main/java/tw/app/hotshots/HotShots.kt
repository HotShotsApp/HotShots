package tw.app.hotshots

import android.app.Application
import android.os.Build
import com.google.android.material.color.DynamicColors
import tw.app.hotshots.logger.LogType
import tw.app.hotshots.logger.Logger
import tw.app.hotshots.settings.Settings
import tw.app.hotshots.util.TimeUtil

class HotShots : Application() {
    override fun onCreate() {
        super.onCreate()

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
    }
}