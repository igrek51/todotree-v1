package igrek.todotree.service.history

class ChangesHistory {

    private var anyChange = false

    fun clear() {
        anyChange = false
    }

    fun registerChange() {
        anyChange = true
    }

    fun hasChanges(): Boolean {
        return anyChange
    }
}