package igrek.todotree.intent

import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.domain.treeitem.TextTreeItem
import igrek.todotree.info.UiInfoService
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.service.access.DatabaseLock
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.ui.treelist.TreeListLayout

class ItemTransformCommand(
    treeManager: LazyInject<TreeManager> = appFactory.treeManager,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    databaseLock: LazyInject<DatabaseLock> = appFactory.databaseLock,
) {
    private val treeManager by LazyExtractor(treeManager)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val databaseLock by LazyExtractor(databaseLock)
    private val treeListLayout: TreeListLayout by LazyExtractor(appFactory.treeListLayout)

    fun splitItem(position: Int) {
        databaseLock.assertUnlocked()
        treeManager.currentItem?.getChild(position)?.let { chosenItem: AbstractTreeItem ->
            if (chosenItem !is TextTreeItem) {
                uiInfoService.showInfo("Only text items can be splitted.")
                return
            }
            val textItem: TextTreeItem = chosenItem
            val itemName = textItem.displayName
            val parts = itemName.split(",").map { it.trim() }
            if (parts.size <= 1) {
                uiInfoService.showInfo("There's only one part, separated by comma.")
                return
            }

            val firstPart = parts.first()
            val lastParts = parts.drop(1)

            textItem.setName(firstPart)

            lastParts.asReversed().forEach { part: String ->
                val newItem = TextTreeItem(part)
                treeManager.addToCurrent(position + 1, newItem)
            }

            treeListLayout.updateItemsList()
            uiInfoService.showInfo("${parts.size} items splitted: $itemName")
        }
    }
}