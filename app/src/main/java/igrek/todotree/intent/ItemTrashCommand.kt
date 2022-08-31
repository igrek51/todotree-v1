package igrek.todotree.intent

import igrek.todotree.dagger.FactoryComponent.inject
import igrek.todotree.domain.treeitem.AbstractTreeItem.getChild
import igrek.todotree.intent.GUICommand.updateItemsList
import igrek.todotree.service.resources.UserInfoService.showInfoCancellable
import igrek.todotree.domain.treeitem.AbstractTreeItem.displayName
import igrek.todotree.intent.GUICommand.showItemsList
import igrek.todotree.ui.GUI.scrollToItem
import igrek.todotree.service.resources.UserInfoService.showInfo
import igrek.todotree.domain.treeitem.LinkTreeItem.target
import igrek.todotree.domain.treeitem.AbstractTreeItem.getParent
import igrek.todotree.domain.treeitem.AbstractTreeItem.removeItself
import igrek.todotree.domain.treeitem.AbstractTreeItem.add
import igrek.todotree.domain.treeitem.LinkTreeItem.displayName
import javax.inject.Inject
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.ui.GUI
import igrek.todotree.service.resources.UserInfoService
import igrek.todotree.service.access.DatabaseLock
import igrek.todotree.service.tree.TreeSelectionManager
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.intent.GUICommand
import igrek.todotree.service.resources.InfoBarClickAction
import java.util.TreeSet
import igrek.todotree.domain.treeitem.LinkTreeItem
import java.lang.Runnable
import igrek.todotree.dagger.DaggerIoc
import igrek.todotree.info.UiInfoService
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory

class ItemTrashCommand(
    treeManager: LazyInject<TreeManager> = appFactory.treeManager,
    gui: LazyInject<GUI> = appFactory.gui,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    databaseLock: LazyInject<DatabaseLock> = appFactory.databaseLock,
    treeSelectionManager: LazyInject<TreeSelectionManager> = appFactory.treeSelectionManager,
) {
    private val treeManager by LazyExtractor(treeManager)
    private val gui by LazyExtractor(gui)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val databaseLock by LazyExtractor(databaseLock)
    private val treeSelectionManager by LazyExtractor(treeSelectionManager)

    fun itemRemoveClicked(position: Int) { // removing locked before going into first element
        lock!!.assertUnlocked()
        if (selectionManager!!.isAnythingSelected) {
            removeSelectedItems(true)
        } else {
            removeItem(position)
        }
    }

    private fun removeItem(position: Int) {
        val removing = treeManager!!.currentItem.getChild(position)
        treeManager!!.removeFromCurrent(position)
        GUICommand().updateItemsList()
        userInfo!!.showInfoCancellable("Item removed: " + removing.displayName) {
            restoreRemovedItem(
                removing,
                position
            )
        }
    }

    private fun restoreRemovedItem(restored: AbstractTreeItem, position: Int) {
        treeManager!!.addToCurrent(position, restored)
        GUICommand().showItemsList()
        gui!!.scrollToItem(position)
        userInfo!!.showInfo("Removed item restored.")
    }

    fun removeSelectedItems(info: Boolean) {
        removeItems(selectionManager!!.selectedItems, info)
    }

    fun removeItems(selectedIds: TreeSet<Int>, info: Boolean) {
        //descending order in order to not overwriting indices when removing
        val iterator = selectedIds.descendingIterator()
        while (iterator.hasNext()) {
            treeManager!!.removeFromCurrent(iterator.next()!!)
        }
        if (info) {
            userInfo!!.showInfo("Items removed: " + selectedIds.size)
        }
        selectionManager!!.cancelSelectionMode()
        GUICommand().updateItemsList()
    }

    fun removeLinkAndTarget(linkPosition: Int, linkItem: LinkTreeItem) {
        lock!!.assertUnlocked()

        // remove link
        treeManager!!.removeFromCurrent(linkPosition)

        // remove target
        val target = linkItem.target
        val restoreTargetAction: Runnable
        restoreTargetAction = if (target != null) {
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
        GUICommand().updateItemsList()
        userInfo!!.showInfoCancellable("Link & item removed: " + linkItem.displayName) {

            // restore target
            restoreTargetAction.run()

            // restore link
            treeManager!!.addToCurrent(linkPosition, linkItem)
            GUICommand().showItemsList()
            gui!!.scrollToItem(linkPosition)
            userInfo!!.showInfo("Removed items restored.")
        }
    }

    init {
        DaggerIoc.factoryComponent.inject(this)
    }
}