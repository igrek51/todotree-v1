package igrek.todotree.domain.treeitem

class CheckboxTreeItem(parent: AbstractTreeItem?, override var displayName: String, var isChecked: Boolean) : AbstractTreeItem(parent) {
    override fun clone(): CheckboxTreeItem {
        return CheckboxTreeItem(null, displayName, isChecked).copyChildren(this)
    }

    override val typeName: String
        get() = "checkbox"

    fun setName(name: String) {
        displayName = name
    }

}