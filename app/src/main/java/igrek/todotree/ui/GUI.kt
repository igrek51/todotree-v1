package igrek.todotree.ui

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import igrek.todotree.R
import igrek.todotree.app.AppData
import igrek.todotree.app.AppState
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.info.errorcheck.SafeClickListener
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.appFactory
import igrek.todotree.intent.ExitCommand
import igrek.todotree.intent.ItemEditorCommand
import igrek.todotree.intent.NavigationCommand
import igrek.todotree.ui.edititem.EditItemLayout
import igrek.todotree.ui.treelist.TreeListLayout
import igrek.todotree.util.mainScope
import kotlinx.coroutines.launch

class GUI {

    private val appCompatActivity: AppCompatActivity by LazyExtractor(appFactory.appCompatActivity)
    private val treeListLayout: TreeListLayout by LazyExtractor(appFactory.treeListLayout)
    private val editItemLayout: EditItemLayout by LazyExtractor(appFactory.editItemLayout)
    private val appData: AppData by LazyExtractor(appFactory.appData)
    private val imm: InputMethodManager by LazyExtractor(appFactory.inputMethodManager)

    private var lvl2TreeView: View? = null
    private var lvl2EditView: View? = null
    private var loadingBar: ProgressBar? = null

    private var actionBar: ActionBar? = null

    private fun setMainContentLayout(layoutResource: Int): View {
        return when (layoutResource) {
            R.layout.component_items_list -> {
                lvl2TreeView?.visibility = View.VISIBLE
                lvl2EditView?.visibility = View.GONE
                lvl2TreeView!!
            }

            R.layout.component_edit_item -> {
                lvl2TreeView?.visibility = View.GONE
                lvl2EditView?.visibility = View.VISIBLE
                lvl2EditView!!
            }

            else -> lvl2TreeView!!
        }
    }

    fun startLoading() {
        loadingBar?.visibility = View.VISIBLE
    }

    fun stopLoading() {
        loadingBar?.visibility = View.GONE
    }

    fun hideSoftKeyboard(window: View) {
        imm.hideSoftInputFromWindow(window.windowToken, 0)
    }

    fun showSoftKeyboard(window: View?) {
        imm.showSoftInput(window, 0)
    }

    fun lazyInit() {
        appCompatActivity.findViewById<Toolbar>(R.id.toolbar1)?.let { toolbar ->
            appCompatActivity.setSupportActionBar(toolbar)
            actionBar = appCompatActivity.supportActionBar
            showBackButton(true)
            toolbar.setNavigationOnClickListener(SafeClickListener {
                NavigationCommand().backClicked()
            })
        }
        appCompatActivity.findViewById<ImageButton>(R.id.save2Button)?.let { save2Button ->
            save2Button.setOnClickListener { ExitCommand().optionSaveAndExit() }
        }
        lvl2TreeView = appCompatActivity.findViewById(R.id.compose_view_tree)
        lvl2EditView = appCompatActivity.findViewById(R.id.compose_view_edit)
        loadingBar = appCompatActivity.findViewById(R.id.loadingBar)
    }

    fun showBackButton(show: Boolean) {
        if (actionBar != null) {
            actionBar!!.setDisplayHomeAsUpEnabled(show)
            actionBar!!.setDisplayShowHomeEnabled(show)
        }
    }

    fun showItemsList() {
        startLoading()
        val layoutView = setMainContentLayout(R.layout.component_items_list)
        appData.state = AppState.ITEMS_LIST
        treeListLayout.showLayout(layoutView)
    }

    fun showEditItemPanel(item: AbstractTreeItem?, parent: AbstractTreeItem) {
        startLoading()
        showBackButton(true)
        val layoutView = setMainContentLayout(R.layout.component_edit_item)
        editItemLayout.setCurrentItem(item, parent)
        editItemLayout.showLayout(layoutView)
    }

    fun scrollToItem(itemIndex: Int) {
        if (itemIndex == 0) {
            mainScope.launch {
                treeListLayout.scrollToPosition(0)
            }
        }
    }

    fun hideSoftKeyboard() {
        editItemLayout.hideKeyboard()
    }

    fun forceKeyboardShow() {
        editItemLayout.showKeyboard()
    }

    fun onEditBackClicked(): Boolean {
        if (editItemLayout.onEditBackClicked()) return true
        ItemEditorCommand().cancelEditedItem()
        return false
    }

    fun requestSaveEditedItem() {
        editItemLayout.onSaveItemClick()
    }

    fun setTitle(title: String?) {
        actionBar?.title = title
    }

    @SuppressLint("SourceLockedOrientationActivity")
    fun rotateScreen() {
        val orientation = appCompatActivity.resources?.configuration?.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            appCompatActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            appCompatActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setOrientationPortrait() {
        val orientation = appCompatActivity.resources?.configuration?.orientation
        if (orientation != Configuration.ORIENTATION_PORTRAIT) {
            appCompatActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
}