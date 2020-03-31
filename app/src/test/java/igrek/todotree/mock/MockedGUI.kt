package igrek.todotree.mock

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.ui.GUI

class MockedGUI(activity: AppCompatActivity?) : GUI(activity) {
    override fun lazyInit() {}
    override fun showItemsList(currentItem: AbstractTreeItem) {
        updateItemsList(currentItem, null, null)
    }

    override fun showEditItemPanel(item: AbstractTreeItem?, parent: AbstractTreeItem?) {}
    override fun showExitScreen(): View? {
        return null
    }

    override fun updateItemsList(currentItem: AbstractTreeItem, items: List<AbstractTreeItem?>?, selectedPositions: Set<Int?>?) {}
    override fun scrollToItem(itemIndex: Int) {}
    override fun scrollToItem(y: Int?, itemIndex: Int) {}
    override fun scrollToPosition(y: Int) {}
    override fun scrollToBottom() {}
    override fun hideSoftKeyboard() {}
    override fun editItemBackClicked(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun setTitle(title: String?) {}
    override val currentScrollPos: Int
        get() = 0

    override fun requestSaveEditedItem() {
        throw UnsupportedOperationException()
    }

    override fun rotateScreen() {
        throw UnsupportedOperationException()
    }

    override fun quickInsertRange() {
        throw UnsupportedOperationException()
    }
}