package tw.app.hotshots.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.FragmentActivity
import com.permissionx.guolindev.PermissionX

class AppPermissions {
    companion object {
        fun request(activity: FragmentActivity) {
            if (Build.VERSION.SDK_INT >= 33) {
                PermissionX.init(activity)
                    .permissions(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO
                    )
                    .request { allGranted, grantedList, deniedList ->

                    }
            } else {
                PermissionX.init(activity)
                    .permissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                    .request { allGranted, grantedList, deniedList ->

                    }
            }
        }

        fun isAllowed(context: Context, listener: RequestPermissions) {
            if (Build.VERSION.SDK_INT >= 33) {
                val isImagesAllowed = checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
                val isVideosAllowed = checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED

                if (isImagesAllowed && isVideosAllowed)
                    listener.onGranted()
                else
                    listener.onDenied()
            } else {
                val isReadAllowed = checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

                if (isReadAllowed)
                    listener.onGranted()
                else
                    listener.onDenied()
            }
        }
    }
}

interface RequestPermissions {
    fun onGranted()

    fun onDenied()
}