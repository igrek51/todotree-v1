package igrek.todotree.intent

import igrek.todotree.info.UiInfoService
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.service.calc.NumericAdder
import igrek.todotree.service.clipboard.SystemClipboardManager
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.service.tree.TreeSelectionManager

class ItemSelectionCommand (
    treeManager: LazyInject<TreeManager> = appFactory.treeManager,
    treeSelectionManager: LazyInject<TreeSelectionManager> = appFactory.treeSelectionManager,
) {
    private val treeManager by LazyExtractor(treeManager)
    private val treeSelectionManager by LazyExtractor(treeSelectionManager)

    private fun selectAllItems(selectedState: Boolean) {
        for (i in 0 until treeManager.currentItem!!.size()) {
            treeSelectionManager.setItemSelected(i, selectedState)
        }
        GUICommand().updateItemsList()
    }

    fun toggleSelectAll() {
        if (treeSelectionManager.selectedItemsCount == treeManager.currentItem!!.size()) {
            deselectAll()
        } else {
            selectAllItems(true)
        }
    }

    fun deselectAll() {
        treeSelectionManager.cancelSelectionMode()
        GUICommand().updateItemsList()
    }

    fun selectedItemClicked(position: Int, checked: Boolean) {
        treeSelectionManager.setItemSelected(position, checked)
        GUICommand().updateItemsList()
    }
}