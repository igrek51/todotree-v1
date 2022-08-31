package igrek.todotree.intent

import android.os.Handler
import dagger.Lazy
import igrek.todotree.info.logger.LoggerFactory.logger
import igrek.todotree.dagger.FactoryComponent.inject
import igrek.todotree.ui.GUI.showExitScreen
import igrek.todotree.app.AppData.isState
import igrek.todotree.ui.GUI.requestSaveEditedItem
import igrek.todotree.service.preferences.Preferences.saveAll
import igrek.todotree.activity.ActivityController.quit
import igrek.todotree.intent.PersistenceCommand.saveDatabase
import igrek.todotree.activity.ActivityController.minimize
import igrek.todotree.info.logger.Logger.info
import igrek.todotree.system.WindowManagerService.keepScreenOn
import igrek.todotree.intent.TreeCommand.navigateToRoot
import igrek.todotree.service.preferences.Preferences.getValue
import igrek.todotree.info.logger.Logger.debug
import javax.inject.Inject
import igrek.todotree.app.AppData
import igrek.todotree.ui.GUI
import igrek.todotree.service.summary.AlarmService
import igrek.todotree.service.access.DatabaseLock
import igrek.todotree.activity.ActivityController
import igrek.todotree.system.WindowManagerService
import igrek.todotree.info.logger.LoggerFactory
import java.lang.Runnable
import igrek.todotree.app.AppState
import igrek.todotree.intent.PersistenceCommand
import igrek.todotree.intent.TreeCommand
import igrek.todotree.service.preferences.PropertyDefinition
import igrek.todotree.intent.GUICommand
import igrek.todotree.dagger.DaggerIoc
import igrek.todotree.service.preferences.Preferences

class ExitCommand {
    @JvmField
	@Inject
    var appData: AppData? = null

    @JvmField
	@Inject
    var gui: GUI? = null

    @JvmField
	@Inject
    var preferences: Preferences? = null

    @JvmField
	@Inject
    var alarmService: AlarmService? = null

    @JvmField
	@Inject
    var lock: DatabaseLock? = null

    @JvmField
	@Inject
    var activityController: ActivityController? = null

    @JvmField
	@Inject
    var windowManagerService: Lazy<WindowManagerService>? = null
    private val logger = LoggerFactory.logger
    fun saveAndExitRequested() {
        // show exit screen and wait for rendered
        gui!!.showExitScreen()
        Handler().post { quickSaveAndExit() }
    }

    fun optionSaveAndExit() {
        if (appData!!.isState(AppState.EDIT_ITEM_CONTENT)) {
            gui!!.requestSaveEditedItem()
        }
        saveAndExitRequested()
    }

    fun exitApp() {
        preferences!!.saveAll()
        activityController!!.quit()
    }

    private fun saveAndExit() {
        PersistenceCommand().saveDatabase()
        alarmService!!.setAlarmAt(alarmService!!.nextMidnight)
        exitApp()
    }

    fun quickSaveAndExit() {
        Handler().post { postQuickSave() }
        activityController!!.minimize()
        logger.info("Quick exiting...")
    }

    private fun postQuickSave() {
        PersistenceCommand().saveDatabase()
        alarmService!!.setAlarmAt(alarmService!!.nextMidnight)
        preferences!!.saveAll()
        windowManagerService!!.get().keepScreenOn(false)
        TreeCommand().navigateToRoot()
        val shouldBeLocked =
            preferences!!.getValue(PropertyDefinition.lockDB, Boolean::class.java)!!
        lock!!.isLocked = shouldBeLocked
        GUICommand().showItemsList()
        logger.debug("Quick exiting done")
    }

    init {
        DaggerIoc.factoryComponent.inject(this)
    }
}