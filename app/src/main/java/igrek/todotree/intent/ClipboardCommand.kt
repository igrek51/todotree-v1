package igrek.todotree.intent

import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.domain.treeitem.LinkTreeItem
import igrek.todotree.domain.treeitem.TextTreeItem
import igrek.todotree.info.UiInfoService
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.service.clipboard.SystemClipboardManager
import igrek.todotree.service.clipboard.TreeClipboardManager
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.service.tree.TreeScrollCache
import igrek.todotree.service.tree.TreeSelectionManager
import igrek.todotree.ui.GUI
import java.util.*

class ClipboardCommand(
    treeManager: LazyInject<TreeManager> = appFactory.treeManager,
    systemClipboardManager: LazyInject<SystemClipboardManager> = appFactory.systemClipboardManager,
    treeClipboardManager: LazyInject<TreeClipboardManager> = appFactory.treeClipboardManager,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    gui: LazyInject<GUI> = appFactory.gui,
    treeSelectionManager: LazyInject<TreeSelectionManager> = appFactory.treeSelectionManager,
    treeScrollCache: LazyInject<TreeScrollCache> = appFactory.treeScrollCache,
) {
    private val treeManager by LazyExtractor(treeManager)
    private val systemClipboardManager by LazyExtractor(systemClipboardManager)
    private val treeClipboardManager by LazyExtractor(treeClipboardManager)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val gui by LazyExtractor(gui)
    private val treeSelectionManager by LazyExtractor(treeSelectionManager)
    private val treeScrollCache by LazyExtractor(treeScrollCache)

    fun copyItems(itemPosistions: Set<Int>, info: Boolean) {
        if (itemPosistions.isNotEmpty()) {
            treeClipboardManager.clearClipboard()
            val currentItem = treeManager.currentItem
            treeClipboardManager.copiedFrom = currentItem
            for (selectedItemId in itemPosistions) {
                currentItem?.getChild(selectedItemId)?.let { selectedItem ->
                    treeClipboardManager.addToClipboard(selectedItem)
                }
            }
            //if one item selected - copying also to system clipboard
            if (treeClipboardManager.clipboardSize == 1) {
                val item = treeClipboardManager.clipboard!![0]
                systemClipboardManager.copyToSystemClipboard(item.displayName)
                if (info) uiInfoService.showInfo("Item copied: " + item.displayName)
            } else {
                if (info) uiInfoService.showInfo("Items copied: " + treeClipboardManager.clipboardSize)
            }
            // deselect items
            if (treeSelectionManager.isAnythingSelected) {
                ItemSelectionCommand().deselectAll()
            }
        } else {
            if (info) uiInfoService.showInfo("No items to copy.")
        }
    }

    fun copySelectedItems() {
        if (treeSelectionManager.isAnythingSelected) {
            copyItems(treeSelectionManager.selectedItems!!, true)
        } else {
            uiInfoService.showInfo("No selected items")
        }
    }

    fun cutItems(itemPosistions: TreeSet<Int>) {
        if (!itemPosistions.isEmpty()) {
            copyItems(itemPosistions, false)
            uiInfoService.showInfo("Items cut: " + itemPosistions.size)
            ItemTrashCommand().removeItems(itemPosistions, false)
        }
    }

    fun cutSelectedItems() {
        if (treeSelectionManager.isAnythingSelected) {
            cutItems(treeSelectionManager.selectedItems!!)
        } else {
            uiInfoService.showInfo("No selected items")
        }
    }

    fun pasteItems(_position: Int) {
        var position = _position
        treeScrollCache.storeScrollPosition(treeManager.currentItem, gui.currentScrollPos)
        if (treeClipboardManager.isClipboardEmpty) {
            val systemClipboard = systemClipboardManager.systemClipboard
            if (systemClipboard != null) {
                //wklejanie 1 elementu z systemowego schowka
                treeManager.addToCurrent(position, TextTreeItem(systemClipboard))
                uiInfoService.showInfo("Item pasted: $systemClipboard")
                GUICommand().updateItemsList()
                treeScrollCache.restoreScrollPosition(treeManager.currentItem)?.let { y ->
                    gui.scrollToPosition(y)
                }
            } else {
                uiInfoService.showInfo("Clipboard is empty.")
            }
        } else {
            for (clipboardItem in treeClipboardManager.clipboard ?: emptyList()) {
                clipboardItem.setParent(treeManager.currentItem)
                treeManager.addToCurrent(position, clipboardItem)
                position++ // next item pasted below
            }
            uiInfoService.showInfo("Items pasted: " + treeClipboardManager.clipboardSize)
            treeClipboardManager.recopyClipboard()
            GUICommand().updateItemsList()
            treeScrollCache.restoreScrollPosition(treeManager.currentItem)?.let { y ->
                gui.scrollToPosition(y)
            }
        }
    }

    fun pasteItemsAsLink(_position: Int) {
        var position = _position
        treeScrollCache.storeScrollPosition(treeManager.currentItem, gui.currentScrollPos)
        if (treeClipboardManager.isClipboardEmpty) {
            uiInfoService.showInfo("Clipboard is empty.")
        } else {
            for (clipboardItem in treeClipboardManager.clipboard ?: emptyList()) {
                treeManager.addToCurrent(position, buildLinkItem(clipboardItem))
                position++ // next item pasted below
            }
            uiInfoService.showInfo("Items pasted as links: " + treeClipboardManager.clipboardSize)
            GUICommand().updateItemsList()
            treeScrollCache.restoreScrollPosition(treeManager.currentItem)?.let { y ->
                gui.scrollToPosition(y)
            }
        }
    }

    private fun buildLinkItem(clipboardItem: AbstractTreeItem): AbstractTreeItem {
        if (clipboardItem is LinkTreeItem) { // making link to link
            return clipboardItem.clone()
        }
        val link = LinkTreeItem(treeManager.currentItem, "", null)
        link.setTarget(treeClipboardManager.copiedFrom!!, clipboardItem.displayName)
        return link
    }
}