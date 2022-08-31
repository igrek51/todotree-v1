package igrek.todotree.system

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory

class PackageInfoService(
    context: LazyInject<Context> = appFactory.context,
) {
    private val logger = LoggerFactory.logger
    var versionName: String? = null
        private set
    var versionCode: Long = 0
        private set

    init {
        try {
            val pInfo = context.get().packageManager
                    .getPackageInfo(context.get().packageName, 0)
            versionName = pInfo.versionName
            versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode
            } else {
                pInfo.versionCode.toLong()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            logger.error(e)
        }

    }
}
