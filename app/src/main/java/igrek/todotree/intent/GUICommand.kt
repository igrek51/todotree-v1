package igrek.todotree.intent

import igrek.todotree.dagger.FactoryComponent.inject
import igrek.todotree.app.AppData.state
import igrek.todotree.ui.GUI.updateItemsList
import igrek.todotree.ui.GUI.showItemsList
import igrek.todotree.ui.GUI.scrollToPosition
import igrek.todotree.ui.GUI.lazyInit
import igrek.todotree.ui.GUI.quickInsertRange
import javax.inject.Inject
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.ui.GUI
import igrek.todotree.app.AppData
import igrek.todotree.service.tree.TreeScrollCache
import igrek.todotree.service.tree.TreeSelectionManager
import igrek.todotree.app.AppState
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.dagger.DaggerIoc

class GUICommand {
    @JvmField
	@Inject
    var treeManager: TreeManager? = null

    @JvmField
	@Inject
    var gui: GUI? = null

    @JvmField
	@Inject
    var appData: AppData? = null

    @JvmField
	@Inject
    var scrollCache: TreeScrollCache? = null

    @JvmField
	@Inject
    var selectionManager: TreeSelectionManager? = null
    fun updateItemsList() {
        appData!!.state = AppState.ITEMS_LIST
        gui!!.updateItemsList(treeManager!!.currentItem, null, selectionManager!!.selectedItems)
    }

    fun showItemsList() {
        appData!!.state = AppState.ITEMS_LIST
        gui!!.showItemsList(treeManager!!.currentItem)
    }

    fun restoreScrollPosition(parent: AbstractTreeItem?) {
        val savedScrollPos = scrollCache!!.restoreScrollPosition(parent)
        if (savedScrollPos != null) {
            gui!!.scrollToPosition(savedScrollPos)
        }
    }

    fun guiInit() {
        gui!!.lazyInit()
    }

    fun numKeyboardHyphenTyped() {
        gui!!.quickInsertRange()
    }

    init {
        DaggerIoc.factoryComponent.inject(this)
    }
}