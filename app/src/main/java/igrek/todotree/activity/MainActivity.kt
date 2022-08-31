package igrek.todotree.activity

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import igrek.todotree.R
import igrek.todotree.info.logger.Logger
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.AppContextFactory
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.util.RetryDelayed


open class MainActivity(
    mainActivityData: LazyInject<MainActivityData> = appFactory.activityData,
) : AppCompatActivity() {
    protected var activityData by LazyExtractor(mainActivityData)

    protected val logger: Logger = LoggerFactory.logger

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            logger.info("Creating Dependencies container...")
            AppContextFactory.createAppContext(this)
            recreateFields() // Workaround for reusing finished activities by Android
            super.onCreate(savedInstanceState)
            activityData.appInitializer.init()
        } catch (t: Throwable) {
            logger.fatal(t)
            throw t
        }
    }

    override fun onNewIntent(intent: Intent?) {
        stdNewIntent(intent)
        logger.debug("new intent received")
        if (activityData.quickAddService.isQuickAddModeEnabled) {
            activityData.quickAddService.isQuickAddModeEnabled = false
            logger.debug("recreating activity")
            recreate()
        } else if (activityData.remotePushService.isRemotePushEnabled) {
            activityData.remotePushService.isRemotePushEnabled = false
            logger.debug("recreating activity")
            recreate()
        }
        null?.let {
            super.onNewIntent(intent)
        }
    }

    private fun recreateFields() {
        activityData = appFactory.activityData.get()
    }

    protected fun stdNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        activityData.activityController.onConfigurationChanged(newConfig)
    }

    override fun onDestroy() {
        super.onDestroy()
        activityData.activityController.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        Handler(Looper.getMainLooper()).post {
            RetryDelayed(10, 500, UninitializedPropertyAccessException::class.java) {
                activityData.activityController.onStart()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        activityData.activityController.onStop()
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        activityData.activityResultDispatcher.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return activityData.optionSelectDispatcher.optionsSelect(item.itemId) || super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (activityData.systemKeyDispatcher.onKeyDown(keyCode))
            return true
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return super.onKeyDown(keyCode, event)
    }

}