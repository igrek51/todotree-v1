package igrek.todotree.ui

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import igrek.todotree.R
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.intent.ExitCommand
import igrek.todotree.intent.NavigationCommand
import igrek.todotree.ui.edititem.EditItemGUI
import igrek.todotree.ui.errorcheck.SafeClickListener
import igrek.todotree.ui.treelist.TreeListView

open class GUI(activity: AppCompatActivity?) : BaseGUI(activity) {
    private var actionBar: ActionBar? = null
    private var itemsListView: TreeListView? = null
    private var editItemGUI: EditItemGUI? = null

    open fun lazyInit() {
        activity.setContentView(R.layout.activity_main)

        //toolbar
        val toolbar1 = activity.findViewById<Toolbar>(R.id.toolbar1)
        activity.setSupportActionBar(toolbar1)
        actionBar = activity.supportActionBar
        showBackButton(true)
        toolbar1.setNavigationOnClickListener(object : SafeClickListener() {
            override fun onClick() {
                NavigationCommand().backClicked()
            }
        })
        val save2Button = activity.findViewById<ImageButton>(R.id.save2Button)
        save2Button.setOnClickListener { ExitCommand().optionSaveAndExit() }
        mainContent = activity.findViewById(R.id.mainContent)
    }

    private fun showBackButton(show: Boolean) {
        if (actionBar != null) {
            actionBar!!.setDisplayHomeAsUpEnabled(show)
            actionBar!!.setDisplayShowHomeEnabled(show)
        }
    }

    open fun showItemsList(currentItem: AbstractTreeItem) {
        setOrientationPortrait()
        val itemsListLayout = setMainContentLayout(R.layout.items_list)
        itemsListView = itemsListLayout.findViewById(R.id.treeItemsList)
        itemsListView?.init(activity)
        updateItemsList(currentItem, currentItem.getChildren(), null)
    }

    open fun showEditItemPanel(item: AbstractTreeItem?, parent: AbstractTreeItem?) {
        showBackButton(true)
        // TODO redirect to dedicated views
        editItemGUI = EditItemGUI(this, item, parent)
    }

    open fun showExitScreen(): View? {
        return setMainContentLayout(R.layout.exit_screen)
    }

    open fun updateItemsList(currentItem: AbstractTreeItem, _items: List<AbstractTreeItem?>?, selectedPositions: Set<Int?>?) {
        var items = _items
        if (items == null) items = currentItem.getChildren()

        //tytuł gałęzi
        val sb = StringBuilder(currentItem.displayName)
        if (!currentItem.isEmpty) {
            sb.append(" [")
            sb.append(currentItem.size())
            sb.append("]")
        }
        setTitle(sb.toString())

        // back button visiblity
        showBackButton(currentItem.getParent() != null)

        //lista elementów
        itemsListView!!.setItemsAndSelected(items, selectedPositions)
    }

    open fun scrollToItem(itemIndex: Int) {
        itemsListView!!.scrollToItem(itemIndex)
    }

    open fun scrollToItem(y: Int?, itemIndex: Int) {
        if (y != null) itemsListView!!.scrollToPosition(y)
        itemsListView!!.scrollToItem(itemIndex)
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
        actionBar!!.title = title
    }

    open val currentScrollPos: Int?
        get() = itemsListView!!.currentScrollPosition

    open fun requestSaveEditedItem() {
        editItemGUI!!.requestSaveEditedItem()
    }

    open fun rotateScreen() {
        val orientation = activity.resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
    }

    private fun setOrientationPortrait() {
        val orientation = activity.resources.configuration.orientation
        if (orientation != Configuration.ORIENTATION_PORTRAIT) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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