package igrek.todotree.ui.numkeyboard

interface NumKeyboardListener {
    fun onNumKeyboardClosed()
    fun onSelectionChanged(selStart: Int, selEnd: Int)
}