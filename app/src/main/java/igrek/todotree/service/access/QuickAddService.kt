package igrek.todotree.service.access

import android.app.Activity
import android.os.Handler
import android.view.WindowManager
import igrek.todotree.activity.ActivityController
import igrek.todotree.info.UiInfoService
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.intent.ExitCommand
import igrek.todotree.intent.ItemEditorCommand
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.ui.GUI

class QuickAddService(
    private val activity: LazyInject<Activity> = appFactory.activityMust,
    private val treeManager: LazyInject<TreeManager> = appFactory.treeManager,
    private val gui: LazyInject<GUI> = appFactory.gui,
    private val uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    private val activityController: LazyInject<ActivityController> = appFactory.activityController,
) {
    private val logger = LoggerFactory.logger
    var isQuickAddModeEnabled = false

    fun enableQuickAdd() {
        logger.debug("enabling quick add")
        showOnLockScreen()
        isQuickAddModeEnabled = true
        editNewTmpItem()
        showKeyboard()
    }

    private fun showKeyboard() {
        gui.get().forceKeyboardShow()
        Handler().postDelayed({ gui.get().forceKeyboardShow() }, 300)
    }

    private fun showOnLockScreen() {
        activity.get().window
                .addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
    }

    private fun editNewTmpItem() {
        // go to Tmp
        val tmpItem = treeManager.get().currentItem.findChildByName("Tmp")
        if (tmpItem == null) {
            logger.error("Tmp item was not found")
            uiInfoService.get().showToast("Nowhere to push. Bye!")
            activityController.get().quit()
            return
        }
        treeManager.get().goTo(tmpItem)
        // add item at the end
        ItemEditorCommand().addItemClicked()
    }

    fun exitApp() {
        logger.debug("Exitting quick add mode...")
        ExitCommand().optionSaveAndExit()
    }

}