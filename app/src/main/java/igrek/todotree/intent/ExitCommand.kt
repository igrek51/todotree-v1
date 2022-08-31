package igrek.todotree.intent

import android.os.Handler
import igrek.todotree.activity.ActivityController
import igrek.todotree.app.AppData
import igrek.todotree.app.AppState
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.service.access.DatabaseLock
import igrek.todotree.settings.SettingsState
import igrek.todotree.system.WindowManagerService
import igrek.todotree.ui.GUI

class ExitCommand(
    appData: LazyInject<AppData> = appFactory.appData,
    gui: LazyInject<GUI> = appFactory.gui,
    settingsState: LazyInject<SettingsState> = appFactory.settingsState,
    lock: LazyInject<DatabaseLock> = appFactory.databaseLock,
    activityController: LazyInject<ActivityController> = appFactory.activityController,
    windowManagerService: LazyInject<WindowManagerService> = appFactory.windowManagerService,
) {
    private val appData by LazyExtractor(appData)
    private val gui by LazyExtractor(gui)
    private val settingsState by LazyExtractor(settingsState)
    private val lock by LazyExtractor(lock)
    private val activityController by LazyExtractor(activityController)
    private val windowManagerService by LazyExtractor(windowManagerService)

    private val logger = LoggerFactory.logger

    fun saveAndExitRequested() {
        // show exit screen and wait for rendered
        gui.showExitScreen()
        Handler().post { quickSaveAndExit() }
    }

    fun optionSaveAndExit() {
        if (appData.isState(AppState.EDIT_ITEM_CONTENT)) {
            gui.requestSaveEditedItem()
        }
        saveAndExitRequested()
    }

    fun exitApp() {
        activityController.quit()
    }

    private fun saveAndExit() {
        PersistenceCommand().saveDatabase()
        exitApp()
    }

    fun quickSaveAndExit() {
        Handler().post { postQuickSave() }
        activityController.minimize()
        logger.info("Quick exiting...")
    }

    private fun postQuickSave() {
        PersistenceCommand().saveDatabase()
        windowManagerService.keepScreenOn(false)
        TreeCommand().navigateToRoot()
        val shouldBeLocked = settingsState.lockDB
        lock.isLocked = shouldBeLocked
        GUICommand().showItemsList()
        logger.debug("Quick exiting done")
    }
}