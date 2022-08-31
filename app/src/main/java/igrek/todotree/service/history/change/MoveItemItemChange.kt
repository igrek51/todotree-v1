package igrek.todotree.service.history.change

import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.service.history.change.AbstractItemChange

class MoveItemItemChange(
    val movedItem: AbstractTreeItem,
    val parent: AbstractTreeItem,
    val startPosition: Int,
    val endPosition: Int
) : AbstractItemChange() {
    override fun revert() {
        //TODO
    }
}