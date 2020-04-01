package igrek.todotree.domain.treeitem

class RemoteTreeItem(parent: AbstractTreeItem?, override var displayName: String) : AbstractTreeItem(parent) {

    constructor(name: String) : this(null, name)

    override fun clone(): RemoteTreeItem {
        return RemoteTreeItem(null, displayName).copyChildren(this)
    }

    override val typeName: String
        get() = "remote"

    fun setName(name: String) {
        displayName = name
    }

}