package igrek.todotree.ui.contextmenu

internal abstract class ItemAction(val name: String) {

    abstract fun execute()

    open fun isVisible(): Boolean {
        return true
    }
}