package igrek.todotree.service.clipboard

import igrek.todotree.domain.treeitem.AbstractTreeItem

class TreeClipboardManager {

    var clipboard: MutableList<AbstractTreeItem> = mutableListOf()
    var copiedFrom: AbstractTreeItem? = null
    var markForCut: Boolean = false

    val clipboardSize: Int get() = clipboard.size
    val isClipboardEmpty: Boolean get() = clipboard.isEmpty()

    fun clearClipboard() {
        clipboard.clear()
    }

    fun addToClipboard(item: AbstractTreeItem) {
        clipboard.add(item)
    }

    fun recopyClipboard() {
        val newClipboard = ArrayList<AbstractTreeItem>()
        for (item in clipboard) {
            newClipboard.add(item.clone())
        }
        clipboard = newClipboard
    }
}