package igrek.todotree.service.tree

import igrek.todotree.domain.treeitem.AbstractTreeItem.size
import igrek.todotree.domain.treeitem.AbstractTreeItem.getChild
import igrek.todotree.domain.treeitem.AbstractTreeItem.remove
import igrek.todotree.domain.treeitem.AbstractTreeItem.add
import igrek.todotree.domain.treeitem.AbstractTreeItem
import java.lang.IllegalArgumentException

class TreeMover {
    private fun replace(parent: AbstractTreeItem, pos1: Int, pos2: Int) {
        if (pos1 == pos2) return
        require(!(pos1 < 0 || pos2 < 0)) { "position < 0" }
        require(!(pos1 >= parent.size() || pos2 >= parent.size())) { "position >= size" }
        val item1 = parent.getChild(pos1)
        val item2 = parent.getChild(pos2)
        //wstawienie na pos1
        parent.remove(pos1)
        parent.add(pos1, item2)
        //wstawienie na pos2
        parent.remove(pos2)
        parent.add(pos2, item1)
    }

    /**
     * przesuwa element z pozycji o jedną pozycję w górę
     * @param parent   przodek przesuwanego elementu
     * @param position pozycja elementu przed przesuwaniem
     */
    private fun moveUp(parent: AbstractTreeItem, position: Int) {
        if (position <= 0) return
        replace(parent, position, position - 1)
    }

    /**
     * przesuwa element z pozycji o jedną pozycję w dół
     * @param parent   przodek przesuwanego elementu
     * @param position pozycja elementu przed przesuwaniem
     */
    private fun moveDown(parent: AbstractTreeItem, position: Int) {
        if (position >= parent.size() - 1) return
        replace(parent, position, position + 1)
    }

    /**
     * przesuwa element z pozycji o określoną liczbę pozycji
     * @param parent   przodek przesuwanego elementu
     * @param position pozycja elementu przed przesuwaniem
     * @param step     liczba pozycji do przesunięcia (dodatnia - w dół, ujemna - w górę)
     * @return nowa pozycja elementu
     */
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