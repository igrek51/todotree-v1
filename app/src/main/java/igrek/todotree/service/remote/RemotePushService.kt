package igrek.todotree.service.remote

import android.app.Activity
import android.os.Handler
import android.view.WindowManager
import dagger.Lazy
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.intent.ExitCommand
import igrek.todotree.intent.ItemEditorCommand
import igrek.todotree.service.resources.UserInfoService
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.ui.GUI
import io.reactivex.android.schedulers.AndroidSchedulers

class RemotePushService(
        private val activity: Activity,
        private val treeManager: TreeManager,
        private val gui: Lazy<GUI>,
        private val userInfoService: UserInfoService,
        private val remoteDbRequester: RemoteDbRequester,
) {
    private val logger = LoggerFactory.logger
    var isRemotePushingEnabled = false

    fun enableRemotePush() {
        logger.debug("enabling remote push")
        showOnLockScreen()
        isRemotePushingEnabled = true
        editNewPushItem()
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

    private fun editNewPushItem() {
        treeManager.reset()
        // add item at the end
        ItemEditorCommand().addItemClicked()
    }

    fun exitApp() {
        logger.debug("Exitting quick add mode...")
        ExitCommand().optionSaveAndExit()
    }

    fun pushAndExit(content: String?) {
        userInfoService.showToast("Pushing...")

        remoteDbRequester.createRemoteTodo(content ?: "")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    userInfoService.showToast("Success!")
                    exitApp()
                }, {
                    userInfoService.showToast("Communication breakdown!")
                })
    }

}