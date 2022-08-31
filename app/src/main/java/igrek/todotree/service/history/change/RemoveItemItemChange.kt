package igrek.todotree.service.history.change

import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.service.history.change.AbstractItemChange

class RemoveItemItemChange(val removedItem: AbstractTreeItem, val parent: AbstractTreeItem) :
    AbstractItemChange() {
    override fun revert() {
    }
}