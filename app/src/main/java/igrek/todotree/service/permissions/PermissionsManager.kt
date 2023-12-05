package igrek.todotree.service.permissions

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Environment
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import igrek.todotree.activity.MainActivity
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.appFactory


class PermissionsManager (
    val context: Context,
) {
    private val appCompatActivity: AppCompatActivity by LazyExtractor(appFactory.appCompatActivity)

    fun setupFiles() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                appCompatActivity.startActivity(Intent(context, MainActivity::class.java))
            } else { //request for the permission
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri: Uri = Uri.fromParts("package", context.packageName, null)
                intent.data = uri
                appCompatActivity.startActivity(intent)
            }
        } else {
            //below android 11=======
            appCompatActivity.startActivity(Intent(context, MainActivity::class.java))
            ActivityCompat.requestPermissions(
                appCompatActivity,
                arrayOf(WRITE_EXTERNAL_STORAGE),
                7
            )
        }
    }

}