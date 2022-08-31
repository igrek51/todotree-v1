package igrek.todotree.intent

import android.os.Handler
import android.os.Looper
import igrek.todotree.domain.treeitem.LinkTreeItem
import igrek.todotree.info.UiInfoService
import igrek.todotree.info.logger.LoggerFactory.logger
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.service.remote.RemotePushService
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.service.tree.TreeSelectionManager
import kotlinx.coroutines.*
import java.util.*

@OptIn(DelicateCoroutinesApi::class)
class ItemActionCommand(
    treeManager: LazyInject<TreeManager> = appFactory.treeManager,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    treeSelectionManager: LazyInject<TreeSelectionManager> = appFactory.treeSelectionManager,
    remotePushService: LazyInject<RemotePushService> = appFactory.remotePushService,
) {
    private val treeManager by LazyExtractor(treeManager)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val treeSelectionManager by LazyExtractor(treeSelectionManager)
    private val remotePushService by LazyExtractor(remotePushService)

    fun actionSelect(position: Int) {
        if (treeManager.isPositionBeyond(position)) {
            uiInfoService.showSnackbar("Could not select")
        } else {
            TreeCommand().itemLongClicked(position)
        }
    }

    fun actionAddAbove(position: Int) {
        //delayed execution due to not showing keyboard
        Handler(Looper.getMainLooper()).post { ItemEditorCommand().addItemHereClicked(position) }
    }

    fun actionCopy(position: Int) {
        val itemPosistions: MutableSet<Int> = TreeSet(treeSelectionManager.selectedItemsNotNull)
        // if nothing selected - include current item
        if (itemPosistions.isEmpty()) {
            itemPosistions.add(position)
        }
        ClipboardCommand().copyItems(itemPosistions, true)
    }

    fun actionPasteAbove(position: Int) {
        Handler(Looper.getMainLooper()).post { ClipboardCommand().pasteItems(position) }
    }

    fun actionPasteAboveAsLink(position: Int) {
        Handler(Looper.getMainLooper()).post { ClipboardCommand().pasteItemsAsLink(position) }
    }

    fun actionCut(position: Int) {
        val itemPosistions = TreeSet(treeSelectionManager.selectedItemsNotNull)
        // if nothing selected - include current item
        if (itemPosistions.isEmpty()) {
            itemPosistions.add(position)
        }
        ClipboardCommand().cutItems(itemPosistions)
    }

    fun actionRemove(position: Int) {
        ItemTrashCommand().itemRemoveClicked(position)
    }

    fun actionRemoveLinkAndTarget(position: Int) {
        if (!treeSelectionManager.isAnythingSelected) {
            val linkItem = treeManager.currentItem?.getChild(position)
            if (linkItem is LinkTreeItem) {
                ItemTrashCommand().removeLinkAndTarget(position, linkItem)
            }
        }
    }

    fun actionSelectAll() {
        ItemSelectionCommand().toggleSelectAll()
    }

    fun actionEdit(position: Int) {
        //delayed execution due to not showing keyboard
        Handler(Looper.getMainLooper()).post {
            treeManager.currentItem?.getChild(position)?.let { item ->
                ItemEditorCommand().itemEditClicked(item)
            }
        }
    }

    fun actionRemoveRemote(position: Int) {
        val itemPosistions: TreeSet<Int> = TreeSet(treeSelectionManager.selectedItemsNotNull)
        if (itemPosistions.isEmpty()) {
            itemPosistions.add(position)
        }
        if (!itemPosistions.isEmpty()) {
            val sortedPositions = itemPosistions.sortedDescending()

            runBlocking {
                GlobalScope.launch(Dispatchers.Main) {
                    val deferredResults: List<Deferred<Result<Unit>>> =
                            sortedPositions.map { position ->
                                remotePushService.removeRemoteItem(position)
                            }

                    val results = deferredResults.map { it.await() }

                    if (results.any { it.isFailure }) {
                        val exceptions = results.filter { it.isFailure }.mapNotNull { it.exceptionOrNull() }
                        exceptions.forEach { logger.error(it) }
                        uiInfoService.showToast("Communication breakdown!")
                    } else {
                        ItemTrashCommand().itemRemoveClicked(sortedPositions[0]) // will remove all selections as well

                        uiInfoService.showToast(when (results.size) {
                            1 -> "Item removed remotely: ${results[0].getOrNull()}"
                            else -> "${results.size} items removed remotely"
                        })
                    }
                }
            }
        }
    }
}