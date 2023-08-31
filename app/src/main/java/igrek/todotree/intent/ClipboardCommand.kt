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
import igrek.todotree.service.tree.TreeSelectionManager
import java.util.TreeSet

class ClipboardCommand(
    treeManager: LazyInject<TreeManager> = appFactory.treeManager,
    systemClipboardManager: LazyInject<SystemClipboardManager> = appFactory.systemClipboardManager,
    treeClipboardManager: LazyInject<TreeClipboardManager> = appFactory.treeClipboardManager,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    treeSelectionManager: LazyInject<TreeSelectionManager> = appFactory.treeSelectionManager,
) {
    private val treeManager by LazyExtractor(treeManager)
    private val systemClipboardManager by LazyExtractor(systemClipboardManager)
    private val treeClipboardManager by LazyExtractor(treeClipboardManager)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val treeSelectionManager by LazyExtractor(treeSelectionManager)

    fun copySelectedItems() {
        if (!treeSelectionManager.isAnythingSelected) {
            uiInfoService.showInfo("No selected items")
            return
        }
        copyItems(treeSelectionManager.selectedItems!!, true)
    }

    fun copyItems(itemPosistions: Set<Int>, info: Boolean, cut: Boolean = false) {
        if (itemPosistions.isEmpty()) {
            if (info)
                uiInfoService.showInfo("No items to copy")
            return
        }
        treeClipboardManager.clearClipboard()
        val currentItem = treeManager.currentItem
        treeClipboardManager.copiedFrom = currentItem
        treeClipboardManager.markForCut = cut
        for (selectedItemId in itemPosistions) {
            currentItem?.getChild(selectedItemId)?.let { selectedItem ->
                treeClipboardManager.addToClipboard(selectedItem)
            }
        }

        if (!cut) {
            val copiedText = treeClipboardManager.clipboard!!.joinToString("\n") {
                it.displayName
            }
            systemClipboardManager.copyToSystemClipboard(copiedText)
        }

        if (info) {
            if (treeClipboardManager.clipboardSize == 1) {
                val item = treeClipboardManager.clipboard!![0]
                uiInfoService.showInfo("Item copied: ${item.displayName}")
            } else {
                uiInfoService.showInfo("Items copied: ${treeClipboardManager.clipboardSize}")
            }
        }

        if (treeSelectionManager.isAnythingSelected) {
            ItemSelectionCommand().deselectAll()
        }
    }

    fun cutSelectedItems() {
        if (!treeSelectionManager.isAnythingSelected) {
            uiInfoService.showInfo("No selected items")
            return
        }
        cutItems(treeSelectionManager.selectedItems!!)
    }

    fun cutItems(itemPosistions: TreeSet<Int>) {
        if (itemPosistions.isEmpty()) return
        copyItems(itemPosistions, false, cut=true)
        uiInfoService.showInfo("Marked for cut: " + itemPosistions.size)
    }

    fun pasteItems(aPosition: Int) {
        var position = aPosition
        if (treeClipboardManager.isClipboardEmpty) {
            // recover by taking text from system clipboard
            val systemClipboard = systemClipboardManager.systemClipboard ?: return run {
                uiInfoService.showInfo("Clipboard is empty.")
            }
            treeManager.addToCurrent(position, TextTreeItem(systemClipboard))
            uiInfoService.showInfo("Item pasted: $systemClipboard")
            GUICommand().updateItemsList()
            return
        }
        when (treeClipboardManager.markForCut) {
            false -> {
                for (clipboardItem in treeClipboardManager.clipboard ?: emptyList()) {
                    clipboardItem.setParent(treeManager.currentItem)
                    treeManager.addToCurrent(position, clipboardItem)
                    position++ // paste next items below
                }
                uiInfoService.showInfo("Items pasted: ${treeClipboardManager.clipboardSize}")
                treeClipboardManager.recopyClipboard()
                GUICommand().updateItemsList()
            }
            true -> {
                for (clipboardItem in treeClipboardManager.clipboard ?: emptyList()) {
                    val oldParent = clipboardItem.getParent()
                    oldParent?.remove(clipboardItem)
                    clipboardItem.setParent(treeManager.currentItem)
                    treeManager.addToCurrent(position, clipboardItem)
                    position++ // paste next items below
                }
                treeClipboardManager.markForCut = false
                uiInfoService.showInfo("Items moved: ${treeClipboardManager.clipboardSize}")
                GUICommand().updateItemsList()
            }
        }
    }

    fun pasteItemsAsLink(aPosition: Int) {
        var position = aPosition
        if (treeClipboardManager.isClipboardEmpty) {
            uiInfoService.showInfo("Clipboard is empty.")
        } else {
            for (clipboardItem in treeClipboardManager.clipboard ?: emptyList()) {
                treeManager.addToCurrent(position, buildLinkItem(clipboardItem))
                position++ // next item pasted below
            }
            uiInfoService.showInfo("Items pasted as links: " + treeClipboardManager.clipboardSize)
            GUICommand().updateItemsList()
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

    fun copyAsText(text: String) {
        systemClipboardManager.copyToSystemClipboard(text)
        treeClipboardManager.clearClipboard()
        uiInfoService.showInfo("Text copied.")
    }
}