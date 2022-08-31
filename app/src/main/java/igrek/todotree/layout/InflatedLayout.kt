package igrek.todotree.layout

import android.content.Context
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import igrek.todotree.R
import igrek.todotree.info.logger.Logger
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.layout.navigation.NavigationMenuController

open class InflatedLayout(
    private val _layoutResourceId: Int,
    context: LazyInject<Context> = appFactory.context,
    private val appCompatActivity: LazyInject<AppCompatActivity?> = appFactory.appCompatActivity,
    layoutController: LazyInject<LayoutController> = appFactory.layoutController,
    navigationMenuController: LazyInject<NavigationMenuController> = appFactory.navigationMenuController,
) : MainLayout {
    protected val context by LazyExtractor(context)
    private val layoutController by LazyExtractor(layoutController)
    private val navigationMenuController by LazyExtractor(navigationMenuController)

    protected val logger: Logger = LoggerFactory.logger

    override fun getLayoutResourceId(): Int {
        return _layoutResourceId
    }

    override fun showLayout(layout: View) {
        setupToolbar(layout)
        setupNavigationMenu(layout)
    }

    private fun setupNavigationMenu(layout: View) {
        layout.findViewById<ImageButton>(R.id.navMenuButton)?.run {
            setOnClickListener { navigationMenuController.navDrawerShow() }
        }
    }

    private fun setupToolbar(layout: View) {
        layout.findViewById<Toolbar>(R.id.toolbar1)?.let { toolbar ->
            appCompatActivity.get()?.setSupportActionBar(toolbar)
            appCompatActivity.get()?.supportActionBar?.run {
                setDisplayHomeAsUpEnabled(false)
                setDisplayShowHomeEnabled(false)
            }
        }
    }

    override fun onBackClicked() {
        layoutController.showPreviousLayoutOrQuit()
    }

    override fun onLayoutExit() {}

    protected fun isLayoutVisible(): Boolean {
        return layoutController.isState(this::class)
    }
}