package igrek.todotree.ui

import android.annotation.SuppressLint
import android.app.Activity
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
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.info.errorcheck.SafeClickListener
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.appFactory
import igrek.todotree.intent.ExitCommand
import igrek.todotree.intent.NavigationCommand
import igrek.todotree.ui.edititem.EditItemGUI
import igrek.todotree.ui.treelist.TreeListView

class GUI {

    private val appCompatActivity: AppCompatActivity by LazyExtractor(appFactory.appCompatActivity)

    private val imm: InputMethodManager? = appCompatActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    private var mainContent: RelativeLayout? = null

    private var actionBar: ActionBar? = null
    private var itemsListView: TreeListView? = null
    private var editItemGUI: EditItemGUI? = null

    fun setMainContentLayout(layoutResource: Int): View {
        mainContent?.removeAllViews()
        val inflater = appCompatActivity.layoutInflater
        val layout = inflater.inflate(layoutResource, null)
        layout.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        mainContent?.addView(layout)
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
        mainContent = appCompatActivity.findViewById(R.id.mainContent)
    }

    private fun showBackButton(show: Boolean) {
        if (actionBar != null) {
            actionBar!!.setDisplayHomeAsUpEnabled(show)
            actionBar!!.setDisplayShowHomeEnabled(show)
        }
    }

    fun showItemsList(currentItem: AbstractTreeItem) {
        setOrientationPortrait()
        val itemsListLayout = setMainContentLayout(R.layout.items_list)
        itemsListView = itemsListLayout.findViewById(R.id.treeItemsList) as TreeListView
        itemsListView?.init(appCompatActivity)
        updateItemsList(currentItem, currentItem.children, null)
    }

    fun showEditItemPanel(item: AbstractTreeItem?, parent: AbstractTreeItem) {
        showBackButton(true)
        editItemGUI = EditItemGUI(this, item, parent)
    }

    fun updateItemsList(currentItem: AbstractTreeItem, mItems: List<AbstractTreeItem>?, selectedPositions: Set<Int>?) {
        var items = mItems
        if (items == null) items = currentItem.children

        val sb = StringBuilder(currentItem.displayName)
        if (!currentItem.isEmpty) {
            sb.append(" [")
            sb.append(currentItem.size())
            sb.append("]")
        }
        setTitle(sb.toString())

        showBackButton(currentItem.getParent() != null)

        itemsListView?.setItemsAndSelected(items, selectedPositions)
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