package igrek.todotree.settings

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import igrek.todotree.R
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.layout.LayoutController
import igrek.todotree.layout.MainLayout

class SettingsLayoutController(
    layoutController: LazyInject<LayoutController> = appFactory.layoutController,
    private val appCompatActivity: LazyInject<AppCompatActivity?> = appFactory.appCompatActivity,
) : MainLayout {
    private val layoutController by LazyExtractor(layoutController)

    override fun showLayout(layout: View) {
        appCompatActivity.get()!!.supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_content, SettingsFragment())
                .commit()
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.screen_settings
    }

    override fun onBackClicked() {
        layoutController.showPreviousLayoutOrQuit()
    }

    override fun onLayoutExit() {}
}
