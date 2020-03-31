package igrek.todotree.domain.treeitem

class SeparatorTreeItem(parent: AbstractTreeItem?) : AbstractTreeItem(parent) {
    override fun clone(): SeparatorTreeItem {
        return SeparatorTreeItem(null).copyChildren(this)
    }

    override val displayName: String
        get() = "----------"

    override val typeName: String
        get() = "separator"
}