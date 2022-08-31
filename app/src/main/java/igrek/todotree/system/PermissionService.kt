package igrek.todotree.system

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory

class PermissionService(
    private val activity: LazyInject<Activity?> = appFactory.activity,
) {
    private val logger = LoggerFactory.logger

    val isStoragePermissionGranted: Boolean
        get() {
            return if (activity.get()!!.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                ActivityCompat.requestPermissions(activity.get()!!, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                false
            }
        }

    private fun onPermissionGranted(permission: String) {
        logger.info("permission $permission has been granted")
    }

    private fun onPermissionDenied(permission: String) {
        logger.warn("permission $permission has been denied")
    }

    fun onRequestPermissionsResult(permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permissions[0])
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                onPermissionDenied(permissions[0])
            }
        }
    }
}
