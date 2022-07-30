package igrek.todotree.activity

import android.app.Activity
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import dagger.Lazy
import igrek.todotree.BuildConfig
import igrek.todotree.dagger.DaggerIoc
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.intent.GUICommand
import igrek.todotree.intent.PersistenceCommand
import igrek.todotree.system.WindowManagerService
import igrek.todotree.ui.ExplosionService
import javax.inject.Inject


class AppInitializer {

    @Inject
    lateinit var windowManagerService: Lazy<WindowManagerService>

    @Inject
    lateinit var activity: Lazy<Activity>

    @Inject
    lateinit var explosionService: Lazy<ExplosionService>

    private val logger = LoggerFactory.logger

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun init() {
        if (BuildConfig.DEBUG) {
            debugInit()
        }

        activity.get().window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        //hide task bar
        if (activity is AppCompatActivity) {
            val appCompatActivity: AppCompatActivity = activity as AppCompatActivity
            appCompatActivity.supportActionBar?.hide()
        }

        GUICommand().guiInit()
        PersistenceCommand().loadRootTree()
        GUICommand().showItemsList()
        explosionService.get().init()

        logger.info("Application has been initialized.")
    }

    private fun debugInit() {
        // Allow showing the activity even if the device is locked
        windowManagerService.get().showAppWhenLocked()
    }

}
