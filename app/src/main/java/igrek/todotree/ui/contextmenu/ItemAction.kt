package igrek.todotree.ui.contextmenu

internal abstract class ItemAction(val name: String) {
    abstract fun execute()
    open val isVisible: Boolean
        get() = true
}