package igrek.todotree.activity

import igrek.todotree.dagger.DaggerIoc
import igrek.todotree.intent.NavigationCommand

class OptionSelectDispatcher {

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun optionsSelect(id: Int): Boolean {
        return NavigationCommand().optionsSelect(id)
    }
}
