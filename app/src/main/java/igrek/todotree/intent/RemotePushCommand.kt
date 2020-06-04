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
                    val deferredResults = mutableListOf<Deferred<Result<String>>>()
                    for (selectedItemId in itemPosistions) {
                        val selectedItem = currentItem.getChild(selectedItemId)
                        selectedItem.displayName
                        deferredResults.add(remotePushService.pushNewItemAsync(selectedItem.displayName))
                    }

                    val results = deferredResults.map { it.await() }
                    if (results.any { it.isFailure }) {
                        val exceptions = results.filter { it.isFailure }.mapNotNull { it.exceptionOrNull() }
                        exceptions.forEach { logger.error(it) }
                        userInfoService.showToast("Communication breakdown!")
                    } else {
                        userInfoService.showToast(when (results.size) {
                            1 -> "Entry pushed: ${results[0].getOrNull()}"
                            else -> "${results.size} Entries pushed"
                        })
                    }
                }
            }
        }
    }


}