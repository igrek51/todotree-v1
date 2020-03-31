package igrek.todotree.service.access

import android.app.Activity
import android.os.Handler
import android.view.WindowManager
import dagger.Lazy
import igrek.todotree.activity.ActivityController
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.intent.ExitCommand
import igrek.todotree.intent.ItemEditorCommand
import igrek.todotree.service.resources.UserInfoService
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.ui.GUI

class QuickAddService(
        private val activity: Activity,
        private val treeManager: TreeManager,
        private val gui: Lazy<GUI>,
        private val userInfoService: UserInfoService,
        private val activityController: ActivityController,
) {
    private val logger = LoggerFactory.logger
    var isQuickAddMode = false

    fun enableQuickAdd() {
        logger.debug("enabling quick add")
        showOnLockScreen()
        isQuickAddMode = true
        editNewTmpItem()
        showKeyboard()
    }

    private fun showKeyboard() {
        gui.get().forceKeyboardShow()
        Handler().postDelayed({ gui.get().forceKeyboardShow() }, 300)
    }

    private fun showOnLockScreen() {
        activity.window
                .addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
    }

    private fun editNewTmpItem() {
        // go to Tmp
        val tmpItem = treeManager.currentItem.findChildByName("Tmp")
        if (tmpItem == null) {
            logger.error("Tmp item was not found")
            userInfoService.showToast("Nowhere to push. Bye!")
            activityController.quit()
            return
        }
        treeManager.goTo(tmpItem)
        // add item at the end
        ItemEditorCommand().addItemClicked()
    }

    fun exitApp() {
        logger.debug("Exitting quick add mode...")
        ExitCommand().optionSaveAndExit()
    }

}