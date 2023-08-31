package igrek.todotree.service.clipboard

import igrek.todotree.domain.treeitem.AbstractTreeItem

class TreeClipboardManager {

    var clipboard: MutableList<AbstractTreeItem>? = null
    var copiedFrom: AbstractTreeItem? = null
    var markForCut: Boolean = false

    val clipboardSize: Int
        get() = if (clipboard == null) 0 else clipboard!!.size
    val isClipboardEmpty: Boolean
        get() = clipboard == null || clipboard!!.size == 0

    fun clearClipboard() {
        clipboard = null
    }

    fun addToClipboard(item: AbstractTreeItem) {
        if (clipboard == null) {
            clipboard = ArrayList()
        }
        clipboard!!.add(item.clone())
    }

    fun recopyClipboard() {
        if (clipboard != null) {
            val newClipboard = ArrayList<AbstractTreeItem>()
            for (item in clipboard!!) {
                newClipboard.add(item.clone())
            }
            clipboard = newClipboard
        }
    }
}