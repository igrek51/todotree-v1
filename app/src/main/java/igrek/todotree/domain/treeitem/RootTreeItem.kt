package igrek.todotree.domain.treeitem

class RootTreeItem : AbstractTreeItem(null) {
    override fun clone(): RootTreeItem {
        return RootTreeItem().copyChildren(this)
    }

    override val displayName: String
        get() = "/"

    override val typeName: String
        get() = "/"

    override fun getParent(): AbstractTreeItem? {
        return null
    }
}