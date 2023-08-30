package igrek.todotree.intent

import igrek.todotree.app.AppData
import igrek.todotree.app.AppState
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.ui.GUI
import igrek.todotree.ui.treelist.TreeListLayout

class GUICommand(
    treeManager: LazyInject<TreeManager> = appFactory.treeManager,
    gui: LazyInject<GUI> = appFactory.gui,
    appData: LazyInject<AppData> = appFactory.appData,
) {
    private val treeManager by LazyExtractor(treeManager)
    private val gui by LazyExtractor(gui)
    private val appData by LazyExtractor(appData)
    private val treeListLayout: TreeListLayout by LazyExtractor(appFactory.treeListLayout)

    fun updateItemsList() {
        appData.state = AppState.ITEMS_LIST
        treeListLayout.updateItemsList()
    }

    fun updateOneListItem(position: Int) {
        treeListLayout.updateOneListItem(position)
    }

    fun showItemsList() {
        appData.state = AppState.ITEMS_LIST
        treeManager.currentItem?.let { _ ->
            gui.showItemsList()
        }
    }

}