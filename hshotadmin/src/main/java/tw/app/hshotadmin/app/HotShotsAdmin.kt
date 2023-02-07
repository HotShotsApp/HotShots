package tw.app.hshotadmin.app

import android.app.Application
import com.google.android.material.color.DynamicColors

class HotShotsAdmin : Application() {
    override fun onCreate() {
        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}