package igrek.todotree.intent

import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.domain.treeitem.LinkTreeItem
import igrek.todotree.info.UiInfoService
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.service.access.DatabaseLock
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.service.tree.TreeSelectionManager
import igrek.todotree.ui.treelist.TreeListLayout
import java.util.TreeSet

class ItemTrashCommand(
    treeManager: LazyInject<TreeManager> = appFactory.treeManager,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    databaseLock: LazyInject<DatabaseLock> = appFactory.databaseLock,
    treeSelectionManager: LazyInject<TreeSelectionManager> = appFactory.treeSelectionManager,
) {
    private val treeManager by LazyExtractor(treeManager)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val databaseLock by LazyExtractor(databaseLock)
    private val treeSelectionManager by LazyExtractor(treeSelectionManager)
    private val gui by LazyExtractor(appFactory.gui)
    private val treeListLayout: TreeListLayout by LazyExtractor(appFactory.treeListLayout)

    fun itemRemoveClicked(position: Int) { // removing locked before going into first element
        databaseLock.assertUnlocked()
        if (treeSelectionManager.isAnythingSelected) {
            removeSelectedItems()
        } else {
            removeItem(position)
        }
    }

    private fun removeItem(position: Int) {
        treeManager.currentItem?.getChild(position)?.let { removing ->
            treeManager.removeFromCurrent(position)
            treeListLayout.updateItemsList()
            uiInfoService.showInfoCancellable("Item removed: " + removing.displayName) {
                restoreRemovedItem(
                    removing,
                    position
                )
            }
        }
    }

    private fun restoreRemovedItem(restored: AbstractTreeItem, position: Int) {
        treeManager.addToCurrent(position, restored)
        gui.showItemsList()
        uiInfoService.showInfo("Removed item restored.")
    }

    private fun removeSelectedItems() {
        removeItems(treeSelectionManager.selectedItems!!, true)
    }

    private fun removeItems(selectedIds: TreeSet<Int>, printInfo: Boolean) {
        //descending order in order to not overwriting indices when removing
        val iterator = selectedIds.descendingIterator()
        while (iterator.hasNext()) {
            treeManager.removeFromCurrent(iterator.next()!!)
        }
        if (printInfo) {
            uiInfoService.showInfo("Items removed: " + selectedIds.size)
        }
        treeSelectionManager.cancelSelectionMode()
        treeListLayout.updateItemsList()
    }

    fun removeLinkAndTarget(linkPosition: Int, linkItem: LinkTreeItem) {
        databaseLock.assertUnlocked()

        // remove link
        treeManager.removeFromCurrent(linkPosition)

        // remove target
        val target = linkItem.target
        val restoreTargetAction: Runnable = if (target != null) {
            val parent = target.getParent()
            val removedIndex = target.removeItself()
            Runnable {
                if (removedIndex >= 0) {
                    parent!!.add(removedIndex, target)
                }
            }
        } else {
            Runnable {}
        }
        treeListLayout.updateItemsList()
        uiInfoService.showInfoCancellable("Link & item removed: " + linkItem.displayName) {

            // restore target
            restoreTargetAction.run()

            // restore link
            treeManager.addToCurrent(linkPosition, linkItem)
            gui.showItemsList()
            uiInfoService.showInfo("Removed items restored.")
        }
    }
}