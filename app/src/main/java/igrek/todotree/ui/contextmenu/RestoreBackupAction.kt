package igrek.todotree.ui.contextmenu

abstract class RestoreBackupAction internal constructor(val name: String) {
    abstract fun execute()
}