package igrek.todotree.activity

import igrek.todotree.intent.NavigationCommand

class OptionSelectDispatcher {

    fun optionsSelect(id: Int): Boolean {
        return NavigationCommand().optionsSelect(id)
    }
}
