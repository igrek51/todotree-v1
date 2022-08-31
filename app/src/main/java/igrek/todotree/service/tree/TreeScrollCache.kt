package igrek.todotree.service.tree

import java.util.HashMap
import igrek.todotree.domain.treeitem.AbstractTreeItem

class TreeScrollCache {

    private val storedScrollPositions: HashMap<AbstractTreeItem, Int> = HashMap()

    fun storeScrollPosition(item: AbstractTreeItem?, y: Int?) {
        if (item != null && y != null) {
            storedScrollPositions[item] = y
        }
    }

    fun restoreScrollPosition(item: AbstractTreeItem?): Int? {
        if (item == null)
            return null
        return storedScrollPositions[item]
    }

    fun clear() {
        storedScrollPositions.clear()
    }

}