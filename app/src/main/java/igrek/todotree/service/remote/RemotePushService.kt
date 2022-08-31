package igrek.todotree.service.remote

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import igrek.todotree.domain.treeitem.RemoteTreeItem
import igrek.todotree.domain.treeitem.TextTreeItem
import igrek.todotree.info.UiInfoService
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.intent.ItemEditorCommand
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.ui.GUI
import kotlinx.coroutines.*

class RemotePushService(
    private val activity: LazyInject<Activity> = appFactory.activityMust,
    private val treeManager: LazyInject<TreeManager> = appFactory.treeManager,
    private val gui: LazyInject<GUI> = appFactory.gui,
    private val uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    private val remoteDbRequester: LazyInject<RemoteDbRequester> = appFactory.remoteDbRequester,
) {
    private val logger = LoggerFactory.logger
    var isRemotePushEnabled = false
    private val remoteItemToId = hashMapOf<TextTreeItem, Long>()

    fun enableRemotePush() {
        logger.debug("enabling remote push")
        showOnLockScreen()
        isRemotePushEnabled = true
        editNewPushItem()
        showKeyboard()
    }

    private fun showKeyboard() {
        gui.get().forceKeyboardShow()
        Handler(Looper.getMainLooper()).postDelayed({ gui.get().forceKeyboardShow() }, 300)
    }

    private fun showOnLockScreen() {
        activity.get().window
                .addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
    }

    private fun editNewPushItem() {
        // add item at the end
        ItemEditorCommand().addItemClicked()
    }

    fun pushAndExitAsync(content: String?): Deferred<Result<String>> {
        if (content.isNullOrBlank()) {
            uiInfoService.get().showToast("Nothing to do")
            return GlobalScope.async { Result.success(content.orEmpty()) }
        }

        GlobalScope.launch(Dispatchers.Main) {
            uiInfoService.get().showSnackbar("Pushing...")
        }

        return remoteDbRequester.get().createRemoteTodo(content)
    }

    fun pushNewItemAsync(content: String): Deferred<Result<String>> {
        if (content.isBlank()) {
            uiInfoService.get().showToast("Nothing to do")
            return GlobalScope.async { Result.success(content) }
        }
        GlobalScope.launch(Dispatchers.Main) {
            uiInfoService.get().showSnackbar("Pushing...")
        }

        return remoteDbRequester.get().createRemoteTodo(content)
    }

    fun pushNewItemsAsync(contents: List<String>): Deferred<Result<Unit>> {
        if (contents.isEmpty()) {
            uiInfoService.get().showToast("Nothing to do")
            return GlobalScope.async { Result.success(Unit) }
        }
        GlobalScope.launch(Dispatchers.Main) {
            uiInfoService.get().showSnackbar("Pushing...")
        }

        return remoteDbRequester.get().createManyRemoteTodos(contents)
    }

    fun populateRemoteItemAsync(item: RemoteTreeItem): Deferred<Result<List<TodoDto>>> {
        // clear current children
        repeat(item.getChildren().size) {
            item.remove(0)
        }
        return GlobalScope.async {
            val dr = remoteDbRequester.get().fetchAllRemoteTodos()
            val result = dr.await()
            result.onSuccess { todoDtos ->
                withContext(Dispatchers.Main) {
                    populateFetchedRemoteItemsId(item, todoDtos)
                }
            }
            result
        }
    }

    private fun populateFetchedRemoteItemsId(remoteItem: RemoteTreeItem, todoDtos: List<TodoDto>) {
        remoteItemToId.clear()
        todoDtos.forEach { todoDto ->
            val newItem = TextTreeItem(todoDto.content ?: "")
            remoteItem.add(newItem)
            todoDto.id?.let { remoteItemToId[newItem] = it }
        }
    }

    fun removeRemoteItem(position: Int): Deferred<Result<Unit>> {
        val item = treeManager.get().getChild(position)
        val itemId = remoteItemToId[item]
        itemId ?: run {
            return GlobalScope.async {
                Result.failure(RuntimeException("remote item ID not found"))
            }
        }
        return remoteDbRequester.get().deleteRemoteTodo(itemId)
    }

}