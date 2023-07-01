package tw.app.hotshots

import android.app.Application
import com.google.android.material.color.DynamicColors
import tw.app.hotshots.logger.Logger

class HotShots : Application() {

    private lateinit var logger: Logger

    override fun onCreate() {
        super.onCreate()

        logger = Logger("HotShots.kt")

        logger.LogD("onCreate")
        logger.LogD("IsDynamicColorAvailable: ${DynamicColors.isDynamicColorAvailable()}")

        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}