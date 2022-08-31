package igrek.todotree.activity

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.intent.PersistenceCommand
import igrek.todotree.persistence.user.UserDataDao
import igrek.todotree.settings.SettingsService
import igrek.todotree.system.WindowManagerService

class ActivityController(
    windowManagerService: LazyInject<WindowManagerService> = appFactory.windowManagerService,
    settingsService: LazyInject<SettingsService> = appFactory.settingsService,
    userDataDao: LazyInject<UserDataDao> = appFactory.userDataDao,
    private val activity: LazyInject<Activity?> = appFactory.activity,
) {
    private val windowManagerService by LazyExtractor(windowManagerService)
    private val preferencesService by LazyExtractor(settingsService)
    private val userDataDao by LazyExtractor(userDataDao)

    private val logger = LoggerFactory.logger
    var initialized = false
    var exitingDiscardingChanges = false

    fun onConfigurationChanged(newConfig: Configuration) {
        // resize event
        val screenWidthDp = newConfig.screenWidthDp
        val screenHeightDp = newConfig.screenHeightDp
        val orientationName = getOrientationName(newConfig.orientation)
        logger.debug("Screen resized: " + screenWidthDp + "dp x " + screenHeightDp + "dp - " + orientationName)
    }

    private fun getOrientationName(orientation: Int): String {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return "landscape"
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return "portrait"
        }
        return orientation.toString()
    }

    fun quit() {
        windowManagerService.keepScreenOn(false)
        activity.get()?.finish()
    }

    fun onStart() {
        if (initialized) {
            logger.debug("starting activity...")
            userDataDao.requestSave(false)
        }
    }

    fun onStop() {
        if (initialized) {
            logger.debug("stopping activity...")
            if (exitingDiscardingChanges) {
                exitingDiscardingChanges = false
            } else {
                PersistenceCommand().saveDatabase()
            }
            preferencesService.saveAll()
            userDataDao.requestSave(true)
        }
    }

    fun onDestroy() {
        if (initialized) {
            preferencesService.saveAll()
            userDataDao.saveNow()
            logger.info("activity has been destroyed")
        }
    }

    fun minimize() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity.get()?.startActivity(startMain)
    }

}
