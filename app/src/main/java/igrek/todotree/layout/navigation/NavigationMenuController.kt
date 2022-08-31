package igrek.todotree.layout.navigation

import android.app.Activity
import android.os.Handler
import android.os.Looper
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import igrek.todotree.R
import igrek.todotree.activity.ActivityController
import igrek.todotree.info.errorcheck.SafeExecutor
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.layout.LayoutController
import igrek.todotree.layout.screen.HomeLayoutController
import igrek.todotree.service.system.SoftKeyboardService
import igrek.todotree.settings.SettingsLayoutController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class NavigationMenuController(
    private val activity: LazyInject<Activity?> = appFactory.activity,
    activityController: LazyInject<ActivityController> = appFactory.activityController,
    layoutController: LazyInject<LayoutController> = appFactory.layoutController,
    softKeyboardService: LazyInject<SoftKeyboardService> = appFactory.softKeyboardService,
) {
    private val activityController by LazyExtractor(activityController)
    private val layoutController by LazyExtractor(layoutController)
    private val softKeyboardService by LazyExtractor(softKeyboardService)

    private var drawerLayout: DrawerLayout? = null
    private var navigationView: NavigationView? = null
    private val actionsMap = HashMap<Int, () -> Unit>()
    private val logger = LoggerFactory.logger

    init {
        initOptionActionsMap()
    }

    private fun initOptionActionsMap() {
        actionsMap[R.id.nav_home] = { layoutController.showLayout(HomeLayoutController::class) }
        actionsMap[R.id.nav_settings] = { layoutController.showLayout(SettingsLayoutController::class) }
        actionsMap[R.id.nav_exit] = { activityController.quit() }
    }

    fun init() {
        drawerLayout = activity.get()?.findViewById(R.id.drawer_layout)
        navigationView = activity.get()?.findViewById(R.id.nav_view)

        navigationView?.setNavigationItemSelectedListener { menuItem ->
            GlobalScope.launch(Dispatchers.Main) {
                // set item as selected to persist highlight
                menuItem.isChecked = true
                drawerLayout?.closeDrawers()
                val id = menuItem.itemId
                if (actionsMap.containsKey(id)) {
                    val action = actionsMap[id]
                    // postpone action - smoother navigation hide
                    Handler(Looper.getMainLooper()).post {
                        SafeExecutor {
                            action?.invoke()
                        }
                    }
                } else {
                    logger.warn("unknown navigation item has been selected.")
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    // unhighlight all menu items
                    navigationView?.let { navigationView ->
                        for (id1 in 0 until navigationView.menu.size())
                            navigationView.menu.getItem(id1).isChecked = false
                    }
                }, 500)
            }
            true
        }
    }

    fun navDrawerShow() {
        drawerLayout?.openDrawer(GravityCompat.START)
        softKeyboardService.hideSoftKeyboard()
    }

    fun navDrawerHide() {
        drawerLayout?.closeDrawers()
    }

    fun isDrawerShown(): Boolean {
        return drawerLayout?.isDrawerOpen(GravityCompat.START) ?: false
    }

}
