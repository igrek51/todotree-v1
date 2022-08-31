package igrek.todotree.layout

import android.app.Activity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.transition.Fade
import androidx.transition.Scene
import androidx.transition.Transition
import androidx.transition.TransitionManager
import igrek.todotree.R
import igrek.todotree.activity.ActivityController
import igrek.todotree.info.errorcheck.SafeExecutor
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.layout.screen.HomeLayoutController
import igrek.todotree.layout.screen.HelpLayoutController
import igrek.todotree.layout.screen.HistoryLayoutController
import igrek.todotree.layout.navigation.NavigationMenuController
import igrek.todotree.layout.screen.ProgramsLayoutController
import igrek.todotree.settings.SettingsLayoutController
import igrek.todotree.system.SystemKeyDispatcher
import kotlinx.coroutines.*
import kotlin.reflect.KClass


@OptIn(DelicateCoroutinesApi::class)
class LayoutController(
    private val activity: LazyInject<Activity?> = appFactory.activity,
    navigationMenuController: LazyInject<NavigationMenuController> = appFactory.navigationMenuController,
    activityController: LazyInject<ActivityController> = appFactory.activityController,
    systemKeyDispatcher: LazyInject<SystemKeyDispatcher> = appFactory.systemKeyDispatcher,
    homeLayoutController: LazyInject<HomeLayoutController> = appFactory.homeLayoutController,
    historyLayoutController: LazyInject<HistoryLayoutController> = appFactory.historyLayoutController,
    helpLayoutController: LazyInject<HelpLayoutController> = appFactory.helpLayoutController,
    settingsLayoutController: LazyInject<SettingsLayoutController> = appFactory.settingsLayoutController,
    programsLayoutController: LazyInject<ProgramsLayoutController> = appFactory.programsLayoutController,
) {
    private val navigationMenuController by LazyExtractor(navigationMenuController)
    private val activityController by LazyExtractor(activityController)
    private val systemKeyDispatcher by LazyExtractor(systemKeyDispatcher)

    private lateinit var mainContentLayout: CoordinatorLayout
    private var currentLayout: MainLayout? = null
    private var layoutHistory: MutableList<MainLayout> = mutableListOf()
    private var registeredLayouts: Map<KClass<out MainLayout>, MainLayout> = mapOf(
        HomeLayoutController::class to homeLayoutController.get(),
        HelpLayoutController::class to helpLayoutController.get(),
        SettingsLayoutController::class to settingsLayoutController.get(),
        HistoryLayoutController::class to historyLayoutController.get(),
        ProgramsLayoutController::class to programsLayoutController.get(),
    )
    private val logger = LoggerFactory.logger
    private val layoutCache = hashMapOf<Int, View>()

    fun init() {
        activity.get()?.let { activity ->
            activity.setContentView(R.layout.main_layout)
            mainContentLayout = activity.findViewById(R.id.main_content)
            mainContentLayout.isFocusable = true
            mainContentLayout.isFocusableInTouchMode = true
            mainContentLayout.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN) {
                    return@setOnKeyListener systemKeyDispatcher.onKeyDown(keyCode)
                }
                return@setOnKeyListener false
            }
            navigationMenuController.init()
        }
    }

    fun showLayout(layoutClass: KClass<out MainLayout>, disableReturn: Boolean = false): Job {
        val layoutController = registeredLayouts[layoutClass]
                ?: throw IllegalArgumentException("${layoutClass.simpleName} class not registered as layout")

        if (disableReturn) {
            // remove current layout from history
            if (currentLayout in layoutHistory) {
                layoutHistory.remove(currentLayout)
            }
        }

        layoutController.let {
            if (it in layoutHistory) {
                layoutHistory.remove(it)
            }
            layoutHistory.add(it)
        }

        logger.debug("Showing layout ${layoutClass.simpleName} [${layoutHistory.size} in history]")

        return GlobalScope.launch(Dispatchers.Main) {
            showMainLayout(layoutController)
        }
    }

    private fun showMainLayout(mainLayout: MainLayout) {
        currentLayout?.onLayoutExit()
        currentLayout = mainLayout

        val transition: Transition = Fade()
        transition.duration = 200

        val (properLayoutView, _) = createLayout(mainLayout.getLayoutResourceId())

        val firstTimeView = mainContentLayout.childCount == 0

        mainContentLayout.removeAllViews()
        mainContentLayout.addView(properLayoutView)

        if (!firstTimeView) {
            TransitionManager.go(Scene(mainContentLayout, properLayoutView), transition)
        }

        mainLayout.showLayout(properLayoutView)
        postInitLayout(mainLayout)
    }

    private fun createLayout(layoutResourceId: Int): Pair<View, Boolean> {
        val inflater = activity.get()!!.layoutInflater
        val properLayoutView = inflater.inflate(layoutResourceId, null)
        properLayoutView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutCache[layoutResourceId] = properLayoutView
        return properLayoutView to false
    }

    private fun postInitLayout(currentLayout: MainLayout) {
    }

    fun showPreviousLayoutOrQuit() {
        // remove current layout from last place
        try {
            val last = layoutHistory.last()
            if (last == currentLayout) {
                layoutHistory = layoutHistory.dropLast(1).toMutableList()
            }
        } catch (e: NoSuchElementException) {
        }

        if (layoutHistory.isEmpty()) {
            activityController.quit()
            return
        }

        val previousLayout = layoutHistory.last()
        logger.debug("Showing previous layout ${previousLayout::class.simpleName} [${layoutHistory.size} in history]")
        GlobalScope.launch(Dispatchers.Main) {
            showMainLayout(previousLayout)
        }
    }

    fun isState(compareLayoutClass: KClass<out MainLayout>): Boolean {
        return compareLayoutClass.isInstance(currentLayout)
    }

    fun onBackClicked() {
        if (navigationMenuController.isDrawerShown()) {
            navigationMenuController.navDrawerHide()
            return
        }
        SafeExecutor {
            currentLayout?.onBackClicked()
        }
    }

}
