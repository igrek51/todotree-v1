package igrek.todotree.service.tree

import java.util.HashMap
import igrek.todotree.domain.treeitem.AbstractTreeItem

class TreeScrollCache {
    private val storedScrollPositions: HashMap<AbstractTreeItem, Int>
    fun storeScrollPosition(item: AbstractTreeItem?, y: Int?) {
        if (item != null && y != null) {
            storedScrollPositions[item] = y
        }
    }

    fun restoreScrollPosition(item: AbstractTreeItem): Int? {
        return storedScrollPositions[item]
    }

    fun clear() {
        storedScrollPositions.clear()
    }

    init {
        storedScrollPositions = HashMap()
    }
}