package igrek.todotree.domain.treeitem

class TextTreeItem(parent: AbstractTreeItem?, override var displayName: String) : AbstractTreeItem(parent) {

    constructor(name: String) : this(null, name)

    override fun clone(): TextTreeItem {
        return TextTreeItem(null, displayName).copyChildren(this)
    }

    override val typeName: String
        get() = "text"

    fun setName(name: String) {
        displayName = name
    }

}