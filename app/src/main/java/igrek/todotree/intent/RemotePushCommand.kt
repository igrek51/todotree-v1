package igrek.todotree.intent

import igrek.todotree.dagger.DaggerIoc
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.info.logger.LoggerFactory.logger
import igrek.todotree.service.remote.RemotePushService
import igrek.todotree.service.resources.UserInfoService
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.service.tree.TreeSelectionManager
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject

class RemotePushCommand {
    @Inject
    lateinit var remotePushService: RemotePushService
    @Inject
    lateinit var selectionManager: TreeSelectionManager
    @Inject
    lateinit var treeManager: TreeManager

    @Inject
    lateinit var userInfoService: UserInfoService

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

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
                        userInfoService.showToast(when (contents.size) {
                            1 -> "Entry pushed: ${contents[0]}"
                            else -> "${contents.size} Entries pushed"
                        })
                    }

                }
            }
        }
    }

}