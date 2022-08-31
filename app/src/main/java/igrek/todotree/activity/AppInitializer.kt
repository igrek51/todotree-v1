package igrek.todotree.activity

import android.app.Activity
import android.content.Context
import android.view.WindowManager
import igrek.todotree.BuildConfig
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.intent.GUICommand
import igrek.todotree.intent.PersistenceCommand
import igrek.todotree.layout.LayoutController
import igrek.todotree.layout.MainLayout
import igrek.todotree.layout.screen.HomeLayoutController
import igrek.todotree.persistence.user.UserDataDao
import igrek.todotree.service.import.DatabaseImportFileChooser
import igrek.todotree.service.permissions.PermissionsManager
import igrek.todotree.settings.SettingsState
import igrek.todotree.system.WindowManagerService
import igrek.todotree.ui.ExplosionService
import kotlinx.coroutines.*
import kotlin.reflect.KClass


@OptIn(DelicateCoroutinesApi::class)
class AppInitializer(
    windowManagerService: LazyInject<WindowManagerService> = appFactory.windowManagerService,
    layoutController: LazyInject<LayoutController> = appFactory.layoutController,
    activityController: LazyInject<ActivityController> = appFactory.activityController,
    userDataDao: LazyInject<UserDataDao> = appFactory.userDataDao,
    explosionService: LazyInject<ExplosionService> = appFactory.explosionService,
    context: LazyInject<Context> = appFactory.context,
    settingsState: LazyInject<SettingsState> = appFactory.settingsState,
    private val activity: LazyInject<Activity?> = appFactory.activity,
) {
    private val windowManagerService by LazyExtractor(windowManagerService)
    private val layoutController by LazyExtractor(layoutController)
    private val activityController by LazyExtractor(activityController)
    private val userDataDao by LazyExtractor(userDataDao)
    private val explosionService by LazyExtractor(explosionService)
    private val context by LazyExtractor(context)
    private val settingsState by LazyExtractor(settingsState)

    private val logger = LoggerFactory.logger
    private val startingScreen: KClass<out MainLayout> = HomeLayoutController::class
    private val debugInitEnabled = false

    fun init(postInit: () -> Unit = {}) {
        logger.info("Initializing application...")

        if (debugInitEnabled && BuildConfig.DEBUG)
            debugInit()

        syncInit()

        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                actualInit()
                handleFirstRun()
                postInit()
                activityController.initialized = true
            }
            logger.info("Application has been initialized.")
        }
    }

    private fun syncInit() {
        DatabaseImportFileChooser().init()
    }

    private suspend fun actualInit() {
        userDataDao // load user data
        layoutController.init()
        windowManagerService.hideTaskbar()

        layoutController.showLayout(startingScreen).join()

        activity.get()?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        PersistenceCommand().loadRootTree()
        GUICommand().showItemsList()
        explosionService.init()
    }

    private fun firstRunInit() {
        PermissionsManager(context).setupFiles()
    }

    private fun debugInit() {
        // Allow showing the activity even if the device is locked
        windowManagerService.showAppWhenLocked()
    }

    private fun handleFirstRun() {
        if (settingsState.appExecutionCount == 0L)
            firstRunInit()
        settingsState.appExecutionCount += 1
    }

}
