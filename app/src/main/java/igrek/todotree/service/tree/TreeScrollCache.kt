package igrek.todotree.service.tree

import java.util.HashMap
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.appFactory
import igrek.todotree.ui.treelist.TreeListLayout
import igrek.todotree.util.mainScope
import kotlinx.coroutines.launch

// Single instance
class TreeScrollCache {

    private val treeManager: TreeManager by LazyExtractor(appFactory.treeManager)
    private val treeListLayout: TreeListLayout by LazyExtractor(appFactory.treeListLayout)

    private val storedScrollPositions: HashMap<AbstractTreeItem, Int> = HashMap()

    fun storeScrollPosition() {
        val item: AbstractTreeItem? = treeManager.currentItem
        val y: Int = treeListLayout.state.scrollState.value
        if (item != null) {
            storedScrollPositions[item] = y
        }
    }

    fun restoreScrollPosition() {
        val item: AbstractTreeItem = treeManager.currentItem ?: return
        val y = storedScrollPositions[item] ?: 0
        mainScope.launch {
            treeListLayout.scrollToPosition(y)
        }
    }

    fun clear() {
        storedScrollPositions.clear()
    }

}