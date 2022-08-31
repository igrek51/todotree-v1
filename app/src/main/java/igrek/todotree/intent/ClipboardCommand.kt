package igrek.todotree.intent

import igrek.todotree.domain.treeitem.AbstractTreeItem.getChild
import igrek.todotree.domain.treeitem.AbstractTreeItem.displayName
import igrek.todotree.service.resources.UiInfoService.showInfo
import igrek.todotree.ui.GUI.currentScrollPos
import igrek.todotree.ui.GUI.scrollToPosition
import igrek.todotree.domain.treeitem.AbstractTreeItem.setParent
import igrek.todotree.domain.treeitem.LinkTreeItem.clone
import igrek.todotree.domain.treeitem.LinkTreeItem.setTarget
import javax.inject.Inject
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.service.clipboard.SystemClipboardManager
import igrek.todotree.service.clipboard.TreeClipboardManager
import igrek.todotree.service.resources.UserInfoService
import igrek.todotree.ui.GUI
import igrek.todotree.service.tree.TreeSelectionManager
import igrek.todotree.service.tree.TreeScrollCache
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.intent.ItemSelectionCommand
import java.util.TreeSet
import igrek.todotree.intent.ItemTrashCommand
import igrek.todotree.domain.treeitem.TextTreeItem
import igrek.todotree.intent.GUICommand
import igrek.todotree.domain.treeitem.LinkTreeItem
import igrek.todotree.dagger.DaggerIoc

class ClipboardCommand {
    @JvmField
	@Inject
    var treeManager: TreeManager? = null

    @JvmField
	@Inject
    var systemClipboardManager: SystemClipboardManager? = null

    @JvmField
	@Inject
    var treeClipboardManager: TreeClipboardManager? = null

    @JvmField
	@Inject
    var userInfo: UserInfoService? = null

    @JvmField
	@Inject
    var gui: GUI? = null

    @JvmField
	@Inject
    var selectionManager: TreeSelectionManager? = null

    @JvmField
	@Inject
    var scrollCache: TreeScrollCache? = null
    fun copyItems(itemPosistions: Set<Int?>, info: Boolean) {
        if (!itemPosistions.isEmpty()) {
            treeClipboardManager!!.clearClipboard()
            val currentItem = treeManager!!.currentItem
            treeClipboardManager!!.copiedFrom = currentItem
            for (selectedItemId in itemPosistions) {
                val selectedItem = currentItem.getChild(selectedItemId!!)
                treeClipboardManager!!.addToClipboard(selectedItem)
            }
            //if one item selected - copying also to system clipboard
            if (treeClipboardManager!!.clipboardSize == 1) {
                val item = treeClipboardManager!!.clipboard[0]
                systemClipboardManager!!.copyToSystemClipboard(item.displayName)
                if (info) userInfo!!.showInfo("Item copied: " + item.displayName)
            } else {
                if (info) userInfo!!.showInfo("Items copied: " + treeClipboardManager!!.clipboardSize)
            }
            // deselect items
            if (selectionManager!!.isAnythingSelected) {
                ItemSelectionCommand().deselectAll()
            }
        } else {
            if (info) userInfo!!.showInfo("No items to copy.")
        }
    }

    fun copySelectedItems() {
        if (selectionManager!!.isAnythingSelected) {
            copyItems(selectionManager!!.selectedItems, true)
        } else {
            userInfo!!.showInfo("No selected items")
        }
    }

    fun cutItems(itemPosistions: TreeSet<Int?>) {
        if (!itemPosistions.isEmpty()) {
            copyItems(itemPosistions, false)
            userInfo!!.showInfo("Items cut: " + itemPosistions.size)
            ItemTrashCommand().removeItems(itemPosistions, false)
        }
    }

    fun cutSelectedItems() {
        if (selectionManager!!.isAnythingSelected) {
            cutItems(selectionManager!!.selectedItems)
        } else {
            userInfo!!.showInfo("No selected items")
        }
    }

    fun pasteItems(position: Int) {
        var position = position
        scrollCache!!.storeScrollPosition(treeManager!!.currentItem, gui!!.currentScrollPos)
        if (treeClipboardManager!!.isClipboardEmpty) {
            val systemClipboard = systemClipboardManager!!.systemClipboard
            if (systemClipboard != null) {
                //wklejanie 1 elementu z systemowego schowka
                treeManager!!.addToCurrent(position, TextTreeItem(systemClipboard))
                userInfo!!.showInfo("Item pasted: $systemClipboard")
                GUICommand().updateItemsList()
                gui!!.scrollToPosition(scrollCache!!.restoreScrollPosition(treeManager!!.currentItem))
            } else {
                userInfo!!.showInfo("Clipboard is empty.")
            }
        } else {
            for (clipboardItem in treeClipboardManager!!.clipboard) {
                clipboardItem.setParent(treeManager!!.currentItem)
                treeManager!!.addToCurrent(position, clipboardItem)
                position++ // next item pasted below
            }
            userInfo!!.showInfo("Items pasted: " + treeClipboardManager!!.clipboardSize)
            treeClipboardManager!!.recopyClipboard()
            GUICommand().updateItemsList()
            gui!!.scrollToPosition(scrollCache!!.restoreScrollPosition(treeManager!!.currentItem))
        }
    }

    fun pasteItemsAsLink(position: Int) {
        var position = position
        scrollCache!!.storeScrollPosition(treeManager!!.currentItem, gui!!.currentScrollPos)
        if (treeClipboardManager!!.isClipboardEmpty) {
            userInfo!!.showInfo("Clipboard is empty.")
        } else {
            for (clipboardItem in treeClipboardManager!!.clipboard) {
                treeManager!!.addToCurrent(position, buildLinkItem(clipboardItem))
                position++ // next item pasted below
            }
            userInfo!!.showInfo("Items pasted as links: " + treeClipboardManager!!.clipboardSize)
            GUICommand().updateItemsList()
            gui!!.scrollToPosition(scrollCache!!.restoreScrollPosition(treeManager!!.currentItem))
        }
    }

    private fun buildLinkItem(clipboardItem: AbstractTreeItem): AbstractTreeItem {
        if (clipboardItem is LinkTreeItem) { // making link to link
            return clipboardItem.clone()
        }
        val link = LinkTreeItem(treeManager!!.currentItem, "", null)
        link.setTarget(treeClipboardManager!!.copiedFrom, clipboardItem.displayName)
        return link
    }

    init {
        DaggerIoc.factoryComponent.inject(this)
    }
}