package igrek.todotree.service.tree

import igrek.todotree.domain.treeitem.AbstractTreeItem

class TreeMover {
    private fun replace(parent: AbstractTreeItem, pos1: Int, pos2: Int) {
        if (pos1 == pos2) return
        require(!(pos1 < 0 || pos2 < 0)) { "position < 0" }
        require(!(pos1 >= parent.size() || pos2 >= parent.size())) { "position >= size" }
        val item1 = parent.getChild(pos1)
        val item2 = parent.getChild(pos2)
        parent.remove(pos1)
        parent.add(pos1, item2)
        parent.remove(pos2)
        parent.add(pos2, item1)
    }

    private fun moveUp(parent: AbstractTreeItem, position: Int) {
        if (position <= 0) return
        replace(parent, position, position - 1)
    }

    private fun moveDown(parent: AbstractTreeItem, position: Int) {
        if (position >= parent.size() - 1) return
        replace(parent, position, position + 1)
    }

    fun move(parent: AbstractTreeItem, position: Int, step: Int): Int {
        var position = position
        var targetPosition = position + step
        if (targetPosition < 0) targetPosition = 0
        if (targetPosition >= parent.size()) targetPosition = parent.size() - 1
        while (position < targetPosition) {
            moveDown(parent, position)
            position++
        }
        while (position > targetPosition) {
            moveUp(parent, position)
            position--
        }
        return targetPosition
    }
}