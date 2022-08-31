package igrek.todotree.service.history.change

import igrek.todotree.domain.treeitem.AbstractTreeItem

class AddItemItemChange(
    val newItem: AbstractTreeItem,
    val parent: AbstractTreeItem,
    val insertPosition: Int
) : AbstractItemChange() {
    override fun revert() {
    }
}