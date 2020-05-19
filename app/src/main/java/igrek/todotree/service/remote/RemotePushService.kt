package igrek.todotree.service.remote

import android.app.Activity
import android.os.Handler
import android.view.WindowManager
import dagger.Lazy
import igrek.todotree.domain.treeitem.RemoteTreeItem
import igrek.todotree.domain.treeitem.TextTreeItem
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.intent.ExitCommand
import igrek.todotree.intent.GUICommand
import igrek.todotree.intent.ItemEditorCommand
import igrek.todotree.intent.ItemTrashCommand
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
    private val remoteItemToId = hashMapOf<TextTreeItem, Long>()

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
        // add item at the end
        ItemEditorCommand().addItemClicked()
    }

    fun exitApp() {
        logger.debug("Exitting quick add mode...")
        ExitCommand().optionSaveAndExit()
    }

    fun pushAndExit(content: String?) {
        if (content.isNullOrBlank()) {
            userInfoService.showToast("Nothing to do")
            exitApp()
            return
        }
        userInfoService.showToast("Pushing...")

        remoteDbRequester.createRemoteTodo(content)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    userInfoService.showToast("Success!")
                    exitApp()
                }, { e ->
                    logger.error(e)
                    userInfoService.showToast("Communication breakdown!")
                })
    }

    fun pushNewItem(content: String) {
        if (content.isBlank()) {
            userInfoService.showToast("Nothing to do")
            return
        }
        userInfoService.showToast("Pushing...")

        remoteDbRequester.createRemoteTodo(content)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    userInfoService.showToast("Entry pushed: $content")
                }, { e ->
                    logger.error(e)
                    userInfoService.showToast("Communication breakdown!")
                })
    }

    fun populateRemoteItem(item: RemoteTreeItem) {
        // clear current children
        repeat(item.getChildren().size) {
            item.remove(0)
        }
        remoteDbRequester.fetchAllRemoteTodos()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ todoDtos ->
                    populateFetchedRemoteItems(item, todoDtos)
                }, { e ->
                    logger.error(e)
                    userInfoService.showInfo("Communication breakdown!")
                })
    }

    private fun populateFetchedRemoteItems(remoteItem: RemoteTreeItem, todoDtos: List<TodoDto>) {
        remoteItemToId.clear()
        todoDtos.forEach { todoDto ->
            val newItem = TextTreeItem(todoDto.content ?: "")
            remoteItem.add(newItem)
            todoDto.id?.let { remoteItemToId[newItem] = it }
        }
        if (todoDtos.isEmpty()) {
            userInfoService.showInfo("No remote items")
        } else {
            GUICommand().updateItemsList()
            userInfoService.showInfo("${todoDtos.size} remote items fetched.")
        }
    }

    fun removeRemoteItem(position: Int) {
        val item = treeManager.getChild(position)
        val itemId = remoteItemToId[item]
        itemId ?: run {
            userInfoService.showInfo("remote item ID not found")
        }
        itemId?.let {
            remoteDbRequester.deleteRemoteTodo(it)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        ItemTrashCommand().itemRemoveClicked(position)
                        userInfoService.showInfo("Item removed remotely")
                    }, { e ->
                        logger.error(e)
                        userInfoService.showInfo("Communication breakdown!")
                    })
        }
    }

}