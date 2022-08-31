package igrek.todotree.intent

import igrek.todotree.dagger.DaggerIoc
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.info.UiInfoService
import igrek.todotree.info.logger.LoggerFactory.logger
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.service.remote.RemotePushService
import igrek.todotree.service.resources.UserInfoService
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.service.tree.TreeSelectionManager
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject

class RemotePushCommand(
    treeManager: LazyInject<TreeManager> = appFactory.treeManager,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    remotePushService: LazyInject<RemotePushService> = appFactory.remotePushService,
    treeSelectionManager: LazyInject<TreeSelectionManager> = appFactory.treeSelectionManager,
) {
    private val treeManager by LazyExtractor(treeManager)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val remotePushService by LazyExtractor(remotePushService)
    private val treeSelectionManager by LazyExtractor(treeSelectionManager)

    fun isRemotePushingEnabled(): Boolean {
        return remotePushService.isRemotePushingEnabled
    }

    fun actionPushItemsToRemote(position: Int) {
        val itemPosistions: TreeSet<Int> = TreeSet(selectionManager.selectedItemsNotNull)
        if (itemPosistions.isEmpty()) {
            itemPosistions.add(position)
        }

        if (!itemPosistions.isEmpty()) {
            val currentItem: AbstractTreeItem = treeManager.currentItem

            runBlocking {
                GlobalScope.launch(Dispatchers.Main) {
                    val contents = itemPosistions.map { selectedItemId ->
                        val selectedItem = currentItem.getChild(selectedItemId)
                        selectedItem.displayName
                    }

                    val deferredResult: Deferred<Result<*>> = when (contents.size) {
                        1 -> {
                            remotePushService.pushNewItemAsync(contents[0])
                        }
                        else -> {
                            remotePushService.pushNewItemsAsync(contents)
                        }
                    }

                    val result = deferredResult.await()
                    if (result.isFailure) {
                        result.exceptionOrNull()?.let { exception ->
                            logger.error(exception)
                        }
                        userInfoService.showToast("Communication breakdown!")
                    } else {
                        val message = when (contents.size) {
                            1 -> "Entry pushed: ${contents[0]}"
                            else -> "${contents.size} Entries pushed"
                        }
                        userInfoService.showInfo(message)
                        userInfoService.showToast(message)
                    }

                }
            }
        }
    }

}