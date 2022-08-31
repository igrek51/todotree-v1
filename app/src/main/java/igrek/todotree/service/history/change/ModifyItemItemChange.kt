package igrek.todotree.service.history.change

import igrek.todotree.domain.treeitem.AbstractTreeItem

class ModifyItemItemChange(
    val modifiedItem: AbstractTreeItem,
    val oldContent: String,
    val newContent: String
) : AbstractItemChange() {
    override fun revert() {
    }
}