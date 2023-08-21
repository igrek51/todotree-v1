package igrek.todotree.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.RelativeLayout
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
import igrek.todotree.intent.NavigationCommand
import igrek.todotree.ui.edititem.EditItemGUI
import igrek.todotree.ui.treelist.TreeListLayout
import igrek.todotree.ui.treelist.TreeListView

class GUI {

    private val appCompatActivity: AppCompatActivity by LazyExtractor(appFactory.appCompatActivity)
    private val treeListLayout: TreeListLayout by LazyExtractor(appFactory.treeListLayout)
    private val appData: AppData by LazyExtractor(appFactory.appData)

    private val imm: InputMethodManager? = appCompatActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    private var mainContentLvl2: RelativeLayout? = null

    private var actionBar: ActionBar? = null
    private var itemsListView: TreeListView? = null
    private var editItemGUI: EditItemGUI? = null

    fun setMainContentLayout(layoutResource: Int): View {
        mainContentLvl2?.removeAllViews()
        val inflater = appCompatActivity.layoutInflater
        val layout = inflater.inflate(layoutResource, null)
        layout.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        mainContentLvl2?.addView(layout)
        return layout
    }

    fun hideSoftKeyboard(window: View) {
        imm?.hideSoftInputFromWindow(window.windowToken, 0)
    }

    fun showSoftKeyboard(window: View?) {
        imm?.showSoftInput(window, 0)
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
        mainContentLvl2 = appCompatActivity.findViewById(R.id.main_content_lvl2)
    }

    fun showBackButton(show: Boolean) {
        if (actionBar != null) {
            actionBar!!.setDisplayHomeAsUpEnabled(show)
            actionBar!!.setDisplayShowHomeEnabled(show)
        }
    }

    fun showItemsList() {
        setOrientationPortrait()
        val layoutView = setMainContentLayout(R.layout.component_items_list)
        appData.state = AppState.ITEMS_LIST
        treeListLayout.showLayout(layoutView)
    }

    fun showEditItemPanel(item: AbstractTreeItem?, parent: AbstractTreeItem) {
        showBackButton(true)
        editItemGUI = EditItemGUI(this, item, parent)
    }

    fun scrollToItem(itemIndex: Int) {
        itemsListView?.scrollToItem(itemIndex)
    }

    fun scrollToPosition(y: Int) {
        itemsListView!!.scrollToPosition(y)
    }

    fun scrollToBottom() {
        itemsListView!!.scrollToBottom()
    }

    fun hideSoftKeyboard() {
        editItemGUI!!.hideKeyboards()
    }

    fun editItemBackClicked(): Boolean {
        return editItemGUI!!.editItemBackClicked()
    }

    fun setTitle(title: String?) {
        actionBar?.title = title
    }

    val currentScrollPos: Int?
        get() = itemsListView?.currentScrollPosition

    fun requestSaveEditedItem() {
        editItemGUI!!.requestSaveEditedItem()
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

    fun quickInsertRange() {
        if (editItemGUI != null) {
            editItemGUI!!.quickInsertRange()
        }
    }

    fun forceKeyboardShow() {
        editItemGUI?.forceKeyboardShow()
    }
}