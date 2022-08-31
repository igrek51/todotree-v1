package igrek.todotree.service.history

import java.util.LinkedList
import igrek.todotree.service.history.change.AbstractItemChange

class ChangesHistory {
    private val changes = LinkedList<AbstractItemChange>()
    private var anyChange = false
    fun getChanges(): List<AbstractItemChange> {
        return changes
    }

    fun addChange(change: AbstractItemChange) {
        changes.add(change)
    }

    fun clear() {
        changes.clear()
        anyChange = false
    }

    fun revertLast() {
        if (!changes.isEmpty()) {
            changes.pollLast().revert()
        }
    }

    fun registerChange() {
        anyChange = true
    }

    fun hasChanges(): Boolean {
        return anyChange
    }
}