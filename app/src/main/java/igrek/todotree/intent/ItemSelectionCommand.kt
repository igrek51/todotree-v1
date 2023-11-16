package igrek.todotree.intent

import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.service.tree.TreeSelectionManager
import igrek.todotree.ui.treelist.TreeListLayout

class ItemSelectionCommand (
    treeManager: LazyInject<TreeManager> = appFactory.treeManager,
    treeSelectionManager: LazyInject<TreeSelectionManager> = appFactory.treeSelectionManager,
) {
    private val treeManager by LazyExtractor(treeManager)
    private val treeSelectionManager by LazyExtractor(treeSelectionManager)
    private val treeListLayout: TreeListLayout by LazyExtractor(appFactory.treeListLayout)

    private fun selectAllItems(selectedState: Boolean) {
        for (i in 0 until treeManager.currentItem!!.size()) {
            treeSelectionManager.setItemSelected(i, selectedState)
        }
        treeListLayout.updateItemsList()
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
        treeListLayout.updateItemsList()
    }

    fun selectedItemClicked(position: Int, checked: Boolean) {
        when (treeSelectionManager.setItemSelected(position, checked)) {
            true -> treeListLayout.updateItemsList()
            false -> treeListLayout.updateOneListItem(position)
        }
    }
}