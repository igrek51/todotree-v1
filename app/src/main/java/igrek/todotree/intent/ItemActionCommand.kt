package igrek.todotree.intent

import android.os.Handler
import igrek.todotree.dagger.DaggerIoc
import igrek.todotree.domain.treeitem.LinkTreeItem
import igrek.todotree.info.logger.LoggerFactory.logger
import igrek.todotree.service.remote.RemotePushService
import igrek.todotree.service.resources.UserInfoService
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.service.tree.TreeSelectionManager
import igrek.todotree.ui.GUI
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject

class ItemActionCommand {
    @Inject
    lateinit var treeManager: TreeManager

    @Inject
    lateinit var gui: GUI

    @Inject
    lateinit var selectionManager: TreeSelectionManager

    @Inject
    lateinit var userInfoService: UserInfoService

    @Inject
    lateinit var remotePushService: RemotePushService

    fun actionSelect(position: Int) {
        if (treeManager.isPositionBeyond(position)) {
            userInfoService.showInfo("Could not select")
        } else {
            TreeCommand().itemLongClicked(position)
        }
    }

    fun actionAddAbove(position: Int) {
        //delayed execution due to not showing keyboard
        Handler().post { ItemEditorCommand().addItemHereClicked(position) }
    }

    fun actionCopy(position: Int) {
        val itemPosistions: MutableSet<Int> = TreeSet(selectionManager.selectedItemsNotNull)
        // if nothing selected - include current item
        if (itemPosistions.isEmpty()) {
            itemPosistions.add(position)
        }
        ClipboardCommand().copyItems(itemPosistions, true)
    }

    fun actionPasteAbove(position: Int) {
        Handler().post { ClipboardCommand().pasteItems(position) }
    }

    fun actionPasteAboveAsLink(position: Int) {
        Handler().post { ClipboardCommand().pasteItemsAsLink(position) }
    }

    fun actionCut(position: Int) {
        val itemPosistions = TreeSet(selectionManager.selectedItemsNotNull)
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
        if (!selectionManager.isAnythingSelected) {
            val linkItem = treeManager.currentItem.getChild(position)
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
        Handler().post {
            val item = treeManager.currentItem.getChild(position)
            ItemEditorCommand().itemEditClicked(item)
        }
    }

    fun actionRemoveRemote(position: Int) {
        val itemPosistions: TreeSet<Int> = TreeSet(selectionManager.selectedItemsNotNull)
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
                        userInfoService.showToast("Communication breakdown!")
                    } else {
                        ItemTrashCommand().itemRemoveClicked(sortedPositions[0]) // will remove all selections as well

                        userInfoService.showToast(when (results.size) {
                            1 -> "Item removed remotely: ${results[0].getOrNull()}"
                            else -> "${results.size} items removed remotely"
                        })
                    }
                }
            }
        }
    }

    init {
        DaggerIoc.factoryComponent.inject(this)
    }
}