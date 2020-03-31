package igrek.todotree.activity

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import dagger.Lazy
import igrek.todotree.R
import igrek.todotree.dagger.DaggerIoc
import igrek.todotree.info.logger.Logger
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.intent.WhatTheFuckCommand
import igrek.todotree.service.access.QuickAddService
import igrek.todotree.service.remote.RemotePushService
import igrek.todotree.system.SystemKeyDispatcher
import javax.inject.Inject

open class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var appInitializer: Lazy<AppInitializer>

    @Inject
    lateinit var activityController: Lazy<ActivityController>

    @Inject
    lateinit var quickAddService: QuickAddService

    @Inject
    lateinit var remotePushService: RemotePushService

    @Inject
    lateinit var optionSelectDispatcher: Lazy<OptionSelectDispatcher>

    @Inject
    lateinit var systemKeyDispatcher: Lazy<SystemKeyDispatcher>

    protected var logger: Logger = LoggerFactory.logger

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            // Dagger Container init
            DaggerIoc.init(this)
            super.onCreate(savedInstanceState)
            DaggerIoc.factoryComponent.inject(this)
            appInitializer.get().init()
        } catch (t: Throwable) {
            logger.fatal(t)
            throw t
        }
    }

    override fun onNewIntent(intent: Intent?) {
        stdNewIntent(intent)
        logger.debug("new intent received")
        if (WhatTheFuckCommand().isQuickAddModeEnabled) {
            quickAddService.isQuickAddMode = false
            logger.debug("recreating activity")
            recreate()
        } else if (WhatTheFuckCommand().isRemotePushEnabled) {
            remotePushService.isRemotePushingEnabled = false
            logger.debug("recreating activity")
            recreate()
        }
        null?.let {
            super.onNewIntent(intent)
        }
    }

    protected fun stdNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        activityController.get().onConfigurationChanged(newConfig)
    }

    override fun onStart() {
        super.onStart()
        activityController.get().onStart()
    }

    override fun onStop() {
        super.onStop()
        activityController.get().onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        activityController.get().onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return optionSelectDispatcher.get().optionsSelect(item.itemId) || super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (systemKeyDispatcher.get().onKeyBack())
                    return true
            }
            KeyEvent.KEYCODE_MENU -> {
                if (systemKeyDispatcher.get().onKeyMenu())
                    return true
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (systemKeyDispatcher.get().onVolumeUp())
                    return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (systemKeyDispatcher.get().onVolumeDown())
                    return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return super.onKeyDown(keyCode, event)
    }

}