package igrek.todotree.ui

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import igrek.todotree.R
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.info.errorcheck.SafeClickListener
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.intent.ExitCommand
import igrek.todotree.intent.NavigationCommand
import igrek.todotree.ui.edititem.EditItemGUI
import igrek.todotree.ui.treelist.TreeListView

open class GUI(
    appCompatActivity: LazyInject<AppCompatActivity?> = appFactory.appCompatActivity,
) : BaseGUI(appCompatActivity.get()) {

    private var actionBar: ActionBar? = null
    private var toolbar: Toolbar? = null
    private var itemsListView: TreeListView? = null
    private var editItemGUI: EditItemGUI? = null
    private var hasParent: Boolean = false

    fun lazyInit() {
        activity?.let { activity ->
            activity.findViewById<Toolbar>(R.id.toolbar1)?.let { toolbar ->
                this.toolbar = toolbar
                activity.setSupportActionBar(toolbar)
                this.actionBar = activity.supportActionBar
                showBackButton(false)
                toolbar.setNavigationOnClickListener(SafeClickListener {
                    if (hasParent) {
                        NavigationCommand().backClicked()
                    } else {
                        ExitCommand().optionSaveAndExit()
                    }
                })
            }
//            activity.findViewById<ImageButton>(R.id.save2Button)?.let { save2Button ->
//                save2Button.setOnClickListener { ExitCommand().optionSaveAndExit() }
//            }
            mainContent = activity.findViewById(R.id.mainContent)
        }
    }

    private fun showBackButton(show: Boolean) {
        if (actionBar != null) {
            actionBar!!.setDisplayHomeAsUpEnabled(true)
            actionBar!!.setDisplayShowHomeEnabled(true)
            toolbar?.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
//            toolbar?.let { toolbar ->
//                toolbar.navigationIcon.getPadding()
//                val drawable = AppCompatResources.getDrawable(toolbar.context, R.drawable.save)
//                toolbar.navigationIcon = drawable
//            }
        }
    }

    open fun showItemsList(currentItem: AbstractTreeItem) {
        setOrientationPortrait()
        val itemsListLayout = setMainContentLayout(R.layout.items_list)
        itemsListView = itemsListLayout.findViewById(R.id.treeItemsList) as TreeListView
        itemsListView?.init(activity)
        updateItemsList(currentItem, currentItem.children, null)
    }

    open fun showEditItemPanel(item: AbstractTreeItem?, parent: AbstractTreeItem) {
        showBackButton(true)
        editItemGUI = EditItemGUI(this, item, parent)
    }

    open fun updateItemsList(currentItem: AbstractTreeItem, _items: List<AbstractTreeItem>?, selectedPositions: Set<Int>?) {
        var items = _items
        if (items == null) items = currentItem.children

        val sb = StringBuilder(currentItem.displayName)
        if (!currentItem.isEmpty) {
            sb.append(" [")
            sb.append(currentItem.size())
            sb.append("]")
        }
        setTitle(sb.toString())

        hasParent = currentItem.getParent() != null
        showBackButton(currentItem.getParent() != null)

        itemsListView?.setItemsAndSelected(items, selectedPositions)
    }

    open fun scrollToItem(itemIndex: Int) {
        itemsListView?.scrollToItem(itemIndex)
    }

    open fun scrollToPosition(y: Int) {
        itemsListView!!.scrollToPosition(y)
    }

    open fun scrollToBottom() {
        itemsListView!!.scrollToBottom()
    }

    open fun hideSoftKeyboard() {
        editItemGUI!!.hideKeyboards()
    }

    open fun editItemBackClicked(): Boolean {
        return editItemGUI!!.editItemBackClicked()
    }

    open fun setTitle(title: String?) {
        actionBar?.title = title
    }

    open val currentScrollPos: Int?
        get() = itemsListView?.currentScrollPosition

    open fun requestSaveEditedItem() {
        editItemGUI!!.requestSaveEditedItem()
    }

    @SuppressLint("SourceLockedOrientationActivity")
    open fun rotateScreen() {
        val orientation = activity?.resources?.configuration?.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setOrientationPortrait() {
        val orientation = activity?.resources?.configuration?.orientation
        if (orientation != Configuration.ORIENTATION_PORTRAIT) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    open fun quickInsertRange() {
        if (editItemGUI != null) {
            editItemGUI!!.quickInsertRange()
        }
    }

    fun forceKeyboardShow() {
        editItemGUI?.forceKeyboardShow()
    }
}