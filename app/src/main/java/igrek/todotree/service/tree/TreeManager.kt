package igrek.todotree.service.tree

import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.domain.treeitem.RootTreeItem
import igrek.todotree.exceptions.NoSuperItemException
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.intent.StatisticsCommand
import igrek.todotree.service.history.ChangesHistory

class TreeManager(
    changesHistory: LazyInject<ChangesHistory> = appFactory.changesHistory,
) {
    private val changesHistory by LazyExtractor(changesHistory)
    
    var rootItem: AbstractTreeItem? = null
        set(value) {
            field = value
            this.currentItem = value
        }

    var currentItem: AbstractTreeItem? = null
        private set

    var newItemPosition: Int? = null

    init {
        reset()
    }

    fun reset() {
        rootItem = RootTreeItem()
        currentItem = rootItem
    }

    fun isPositionBeyond(position: Int): Boolean {
        return position >= currentItem!!.size()
    }

    fun isItemAtPosition(position: Int): Boolean {
        return position >= 0 && position < currentItem!!.size()
    }

    fun getChild(position: Int): AbstractTreeItem {
        return currentItem!!.getChild(position)
    }

    fun addToCurrent(_position: Int?, item: AbstractTreeItem) {
        var position = _position
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
}