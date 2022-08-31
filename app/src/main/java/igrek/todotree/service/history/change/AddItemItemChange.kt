package igrek.todotree.service.history.change

import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.service.history.change.AbstractItemChange

class AddItemItemChange(
    val newItem: AbstractTreeItem,
    val parent: AbstractTreeItem,
    val insertPosition: Int
) : AbstractItemChange() {
    override fun revert() {
        //TODO
    }
}