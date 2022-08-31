package igrek.todotree.domain.treeitem

import java.util.*

abstract class AbstractTreeItem(parent: AbstractTreeItem?) {

    private var _parent: AbstractTreeItem?
    var children: MutableList<AbstractTreeItem>
        protected set

    /**
     * copy constructor, creates cloned item detached from parent
     * @param from source item
     */
    protected fun <T : AbstractTreeItem?> copyChildren(from: T): T {
        _parent = null
        children = ArrayList()
        for (sourceChild in from!!.children) {
            val childCopy = sourceChild.clone()
            childCopy._parent = this
            children.add(childCopy)
        }
        return this as T
    }

    abstract fun clone(): AbstractTreeItem

    abstract val displayName: String

    abstract val typeName: String?

    fun setParent(parent: AbstractTreeItem?) {
        this._parent = parent
    }

    open fun getParent(): AbstractTreeItem? {
        return _parent
    }

    fun getChild(index: Int): AbstractTreeItem {
        if (index < 0) throw IndexOutOfBoundsException("index < 0")
        if (index >= children.size) throw IndexOutOfBoundsException("index > items size (${children.size})")
        return children[index]
    }

    fun getChildOrNull(index: Int): AbstractTreeItem? {
        if (index < 0) return null
        if (index >= children.size) return null
        return children[index]
    }

    private fun getChildIndex(child: AbstractTreeItem): Int {
        for (i in children.indices) {
            if (children[i] === child) {
                return i
            }
        }
        return -1
    }

    val indexInParent: Int
        get() = if (_parent == null) -1 else _parent!!.getChildIndex(this)

    val lastChild: AbstractTreeItem?
        get() = if (children.isEmpty()) null else children[children.size - 1]

    fun findChildByName(name: String): AbstractTreeItem? {
        for (child in children) {
            if (child is TextTreeItem && child.displayName == name) return child
        }
        return null
    }

    fun size(): Int {
        return children.size
    }

    val isEmpty: Boolean
        get() = children.isEmpty()

    fun <T : AbstractTreeItem?> add(newItem: T) {
        newItem!!.setParent(this)
        children.add(newItem)
    }

    fun <T : AbstractTreeItem?> add(location: Int, newItem: T) {
        newItem!!.setParent(this)
        children.add(location, newItem)
    }

    fun remove(location: Int) {
        children.removeAt(location)
    }

    fun remove(item: AbstractTreeItem): Boolean {
        return children.remove(item)
    }

    fun removeItself(): Int {
        val indexInParent = indexInParent
        if (indexInParent > -1) {
            getParent()!!.remove(indexInParent)
        }
        return indexInParent
    }

    //except root item
    val namesPaths: MutableList<String?>
        get() {
            val names: MutableList<String?> = ArrayList()
            var current: AbstractTreeItem? = this
            do {
                //except root item
                if (current is RootTreeItem) break
                names.add(current!!.displayName)
                current = current.getParent()
            } while (current != null)
            names.reverse()
            return names
        }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("item: { type: ")
        sb.append(typeName)
        sb.append(", name: ")
        sb.append(displayName)
        if (children.isNotEmpty()) {
            sb.append(", itemsSize: ")
            sb.append(children.size)
        }
        sb.append(" }")
        return sb.toString()
    }

    init {
        children = ArrayList()
        this._parent = parent
    }
}