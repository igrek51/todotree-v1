package igrek.todotree.layout.screen

import android.view.View
import igrek.todotree.R
import igrek.todotree.layout.InflatedLayout
import kotlinx.coroutines.DelicateCoroutinesApi

@OptIn(DelicateCoroutinesApi::class)
class HomeLayoutController(
) : InflatedLayout(
        _layoutResourceId = R.layout.screen_home
) {

    override fun showLayout(layout: View) {
        super.showLayout(layout)
    }

}
