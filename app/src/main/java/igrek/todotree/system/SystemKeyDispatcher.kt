package igrek.todotree.system

import igrek.todotree.intent.NavigationCommand

class SystemKeyDispatcher {

    fun onKeyBack(): Boolean {
        return NavigationCommand().backClicked()
    }

    fun onKeyMenu(): Boolean {
        return false
    }

    fun onVolumeUp(): Boolean {
        return false
    }

    fun onVolumeDown(): Boolean {
        return false
    }
}
