package igrek.todotree.domain.treeitem

import java.util.*

abstract class AbstractTreeItem(parent: AbstractTreeItem?) {

    private var _parent: AbstractTreeItem?
    protected var _children: MutableList<AbstractTreeItem>

    /**
     * copy constructor, creates cloned item detached from parent
     * @param from source item
     */
    protected fun <T : AbstractTreeItem?> copyChildren(from: T): T {
        _parent = null
        _children = ArrayList()
        for (sourceChild in from!!._children) {
            val childCopy = sourceChild.clone()
            childCopy._parent = this
            _children.add(childCopy)
        }
        return this as T
    }

    abstract fun clone(): AbstractTreeItem

    abstract val displayName: String

    abstract val typeName: String?

    fun getChildren(): List<AbstractTreeItem> {
        return _children
    }

    fun setParent(parent: AbstractTreeItem?) {
        this._parent = parent
    }

    open fun getParent(): AbstractTreeItem? {
        return _parent
    }

    fun getChild(index: Int): AbstractTreeItem {
        if (index < 0) throw IndexOutOfBoundsException("index < 0")
        if (index >= _children.size) throw IndexOutOfBoundsException("index > size = " + _children.size)
        return _children[index]
    }

    private fun getChildIndex(child: AbstractTreeItem): Int {
        for (i in _children.indices) {
            if (_children[i] === child) {
                return i
            }
        }
        return -1
    }

    val indexInParent: Int
        get() = if (_parent == null) -1 else _parent!!.getChildIndex(this)

    val lastChild: AbstractTreeItem?
        get() = if (_children.isEmpty()) null else _children[_children.size - 1]

    fun findChildByName(name: String): AbstractTreeItem? {
        for (child in _children) {
            if (child is TextTreeItem && child.displayName == name) return child
        }
        return null
    }

    fun size(): Int {
        return _children.size
    }

    val isEmpty: Boolean
        get() = _children.isEmpty()

    fun <T : AbstractTreeItem?> add(newItem: T) {
        newItem!!.setParent(this)
        _children.add(newItem)
    }

    fun <T : AbstractTreeItem?> add(location: Int, newItem: T) {
        newItem!!.setParent(this)
        _children.add(location, newItem)
    }

    fun remove(location: Int) {
        _children.removeAt(location)
    }

    fun remove(item: AbstractTreeItem): Boolean {
        return _children.remove(item)
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
            Collections.reverse(names)
            return names
        }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("item: { type: ")
        sb.append(typeName)
        sb.append(", name: ")
        sb.append(displayName)
        if (!_children.isEmpty()) {
            sb.append(", itemsSize: ")
            sb.append(_children.size)
        }
        sb.append(" }")
        return sb.toString()
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    init {
        _children = ArrayList()
        this._parent = parent
    }
}