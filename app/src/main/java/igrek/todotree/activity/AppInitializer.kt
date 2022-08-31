package igrek.todotree.activity

import android.app.Activity
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
import igrek.todotree.system.WindowManagerService
import igrek.todotree.ui.ExplosionService
import kotlinx.coroutines.*
import kotlin.reflect.KClass


class AppInitializer(
    windowManagerService: LazyInject<WindowManagerService> = appFactory.windowManagerService,
    layoutController: LazyInject<LayoutController> = appFactory.layoutController,
    activityController: LazyInject<ActivityController> = appFactory.activityController,
    userDataDao: LazyInject<UserDataDao> = appFactory.userDataDao,
    explosionService: LazyInject<ExplosionService> = appFactory.explosionService,
    private val activity: LazyInject<Activity?> = appFactory.activity,
) {
    private val windowManagerService by LazyExtractor(windowManagerService)
    private val layoutController by LazyExtractor(layoutController)
    private val activityController by LazyExtractor(activityController)
    private val userDataDao by LazyExtractor(userDataDao)
    private val explosionService by LazyExtractor(explosionService)

    private val logger = LoggerFactory.logger
    private val startingScreen: KClass<out MainLayout> = HomeLayoutController::class
    private val debugInitEnabled = false

    @OptIn(DelicateCoroutinesApi::class)
    fun init() {
        if (debugInitEnabled && BuildConfig.DEBUG) {
            debugInit()
        }

        logger.info("Initializing application...")


        GlobalScope.launch {
            withContext(Dispatchers.Main) {

                userDataDao // load
                layoutController.init()
                windowManagerService.hideTaskbar()

                layoutController.showLayout(startingScreen).join()

                activity.get()?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

                GUICommand().guiInit()
                PersistenceCommand().loadRootTree()
                GUICommand().showItemsList()
                explosionService.init()

                activityController.initialized = true
            }

            logger.info("Application has been initialized.")
        }


    }

    private fun debugInit() {
        // Allow showing the activity even if the device is locked
        windowManagerService.showAppWhenLocked()
    }

}
