package igrek.todotree.service.tree

import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.domain.treeitem.RootTreeItem
import igrek.todotree.exceptions.NoSuperItemException
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.intent.StatisticsCommand
import igrek.todotree.service.history.ChangesHistory
import java.util.*

class TreeManager(
    changesHistory: LazyInject<ChangesHistory> = appFactory.changesHistory,
) {
    private val changesHistory by LazyExtractor(changesHistory)

    private var rootItem: AbstractTreeItem? = null

    var currentItem: AbstractTreeItem? = null
        private set

    var newItemPosition: Int? = null

    fun reset() {
        rootItem = RootTreeItem()
        currentItem = rootItem
    }

    fun getRootItem(): AbstractTreeItem? {
        return rootItem
    }

    fun setRootItem(rootItem: AbstractTreeItem?) {
        this.rootItem = rootItem
        currentItem = rootItem
    }

    fun isPositionBeyond(position: Int): Boolean {
        return position >= currentItem!!.size()
    }

    fun isPositionAtItem(position: Int): Boolean {
        return position >= 0 && position < currentItem!!.size()
    }

    fun positionAfterEnd(): Int {
        return currentItem!!.size()
    }

    val allChildrenIds: TreeSet<Int>
        get() {
            val ids = TreeSet<Int>()
            for (id in currentItem!!.getChildren().indices) {
                ids.add(id)
            }
            return ids
        }

    fun getChild(position: Int): AbstractTreeItem {
        return currentItem!!.getChild(position)
    }

    fun addToCurrent(position: Int?, item: AbstractTreeItem) {
        var position = position
        if (position == null) {
            position = currentItem!!.size()
        }
        item.setParent(currentItem)
        changesHistory.registerChange()
        currentItem!!.add(position, item)
        StatisticsCommand().onTaskCreated(item)
    }

    fun removeFromCurrent(position: Int) {
        val removingChild = currentItem!!.getChild(position)
        StatisticsCommand().onTaskRemoved(removingChild)
        changesHistory.registerChange()
        currentItem!!.remove(position)
    }

    fun removeFromCurrent(item: AbstractTreeItem?) {
        changesHistory.registerChange()
        StatisticsCommand().onTaskRemoved(item!!)
        currentItem!!.remove(item)
    }

    @Throws(NoSuperItemException::class)
    fun goUp() {
        currentItem = if (currentItem === rootItem) {
            throw NoSuperItemException()
        } else if (currentItem!!.getParent() == null) {
            throw IllegalStateException("parent = null. This should not happen")
        } else {
            currentItem!!.getParent()
        }
    }

    fun goInto(childIndex: Int) {
        val item = currentItem!!.getChild(childIndex)
        goTo(item)
    }

    fun goTo(child: AbstractTreeItem?) {
        currentItem = child
    }

    init {
        reset()
    }
}