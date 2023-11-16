package igrek.todotree.layout.screen

import android.view.View
import igrek.todotree.R
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.intent.NavigationCommand
import igrek.todotree.layout.InflatedLayout
import igrek.todotree.ui.GUI

class HomeLayoutController(
    gui: LazyInject<GUI> = appFactory.gui,
) : InflatedLayout(
    _layoutResourceId = R.layout.screen_home
) {
    private val gui by LazyExtractor(gui)

    override fun showLayout(layout: View) {
        super.showLayout(layout)
        gui.lazyInit()
        gui.showItemsList()
    }

    override fun onBackClicked() {
        NavigationCommand().backClicked()
    }

}
