package igrek.todotree.intent

import igrek.todotree.app.AppData
import igrek.todotree.app.AppState
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.service.tree.TreeScrollCache
import igrek.todotree.ui.GUI
import igrek.todotree.ui.treelist.TreeListLayout

class GUICommand(
    treeManager: LazyInject<TreeManager> = appFactory.treeManager,
    gui: LazyInject<GUI> = appFactory.gui,
    appData: LazyInject<AppData> = appFactory.appData,
    treeScrollCache: LazyInject<TreeScrollCache> = appFactory.treeScrollCache,
) {
    private val treeManager by LazyExtractor(treeManager)
    private val gui by LazyExtractor(gui)
    private val appData by LazyExtractor(appData)
    private val treeScrollCache by LazyExtractor(treeScrollCache)
    private val treeListLayout: TreeListLayout by LazyExtractor(appFactory.treeListLayout)

    fun updateItemsList() {
        appData.state = AppState.ITEMS_LIST
        treeListLayout.updateItemsList()
    }

    fun showItemsList() {
        appData.state = AppState.ITEMS_LIST
        treeManager.currentItem?.let { currentItem ->
            gui.showItemsList()
        }
    }

    fun restoreScrollPosition(parent: AbstractTreeItem?) {
        parent?.let {
            treeScrollCache.restoreScrollPosition(parent)?.let { savedScrollPos ->
                gui.scrollToPosition(savedScrollPos)
            }
        }
    }

    fun numKeyboardHyphenTyped() {
        gui.quickInsertRange()
    }

}