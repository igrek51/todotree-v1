package igrek.todotree.service.history.change

abstract class AbstractItemChange {
    abstract fun revert()
}