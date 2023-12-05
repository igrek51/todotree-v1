package igrek.todotree.ui

import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import igrek.todotree.R
import igrek.todotree.app.AppData
import igrek.todotree.app.AppState
import igrek.todotree.domain.treeitem.AbstractTreeItem
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

    private var lvl2TreeView: View? = null
    private var lvl2EditView: View? = null
    private var loadingBar: ProgressBar? = null

    private var goBackButton: ImageButton? = null
    private var tabTitleLabel: TextView? = null

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

    fun lazyInit() {
        appCompatActivity.findViewById<Toolbar>(R.id.toolbar1)?.let { toolbar ->
            appCompatActivity.setSupportActionBar(toolbar)
            val actionBar = appCompatActivity.supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(false)
            actionBar?.setDisplayShowHomeEnabled(false)
        }
        goBackButton = appCompatActivity.findViewById<ImageButton>(R.id.goBackButton)?.also {
            it.setOnClickListener {
                NavigationCommand().backClicked()
            }
        }
        tabTitleLabel = appCompatActivity.findViewById(R.id.tabTitleLabel)
        appCompatActivity.findViewById<ImageButton>(R.id.save2Button)?.let {
            it.setOnClickListener {
                ExitCommand().saveItemAndExit()
            }
        }
        lvl2TreeView = appCompatActivity.findViewById(R.id.compose_view_tree)
        lvl2EditView = appCompatActivity.findViewById(R.id.compose_view_edit)
        loadingBar = appCompatActivity.findViewById(R.id.loadingBar)
        showBackButton(true)
    }

    fun showBackButton(show: Boolean) {
        goBackButton?.visibility = when (show) {
            true -> View.VISIBLE
            false -> View.INVISIBLE
        }
    }

    fun showItemsList() {
        startLoading()
        val layoutView = setMainContentLayout(R.layout.component_items_list)
        appData.state = AppState.ITEMS_LIST
        treeListLayout.showCachedLayout(layoutView)
    }

    fun showEditItemPanel(item: AbstractTreeItem?, parent: AbstractTreeItem) {
        startLoading()
        showBackButton(true)
        val layoutView = setMainContentLayout(R.layout.component_edit_item)
        editItemLayout.setCurrentItem(item, parent)
        editItemLayout.showCachedLayout(layoutView)
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
        tabTitleLabel?.text = title
    }
}