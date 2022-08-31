package igrek.todotree.intent

import igrek.todotree.info.UiInfoService
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.service.calc.NumericAdder
import igrek.todotree.service.clipboard.SystemClipboardManager
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.service.tree.TreeSelectionManager

class ItemSelectionCommand (
    treeManager: LazyInject<TreeManager> = appFactory.treeManager,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    systemClipboardManager: LazyInject<SystemClipboardManager> = appFactory.systemClipboardManager,
    treeSelectionManager: LazyInject<TreeSelectionManager> = appFactory.treeSelectionManager,
) {
    private val treeManager by LazyExtractor(treeManager)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val systemClipboardManager by LazyExtractor(systemClipboardManager)
    private val treeSelectionManager by LazyExtractor(treeSelectionManager)

    private fun selectAllItems(selectedState: Boolean) {
        for (i in 0 until treeManager.currentItem!!.size()) {
            treeSelectionManager.setItemSelected(i, selectedState)
        }
        GUICommand().updateItemsList()
    }

    fun toggleSelectAll() {
        if (treeSelectionManager.selectedItemsCount == treeManager.currentItem!!.size()) {
            deselectAll()
        } else {
            selectAllItems(true)
        }
    }

    fun deselectAll() {
        treeSelectionManager.cancelSelectionMode()
        GUICommand().updateItemsList()
    }

    fun sumItems() {
        val itemIds: Set<Int> = if (treeSelectionManager.isAnythingSelected) {
            treeSelectionManager.selectedItems!!
        } else {
            treeManager.allChildrenIds
        }
        try {
            val sum = NumericAdder().calculateSum(itemIds, treeManager.currentItem!!)
            var clipboardStr = sum.toPlainString()
            clipboardStr = clipboardStr.replace('.', ',')
            systemClipboardManager.copyToSystemClipboard(clipboardStr)
            uiInfoService.showInfo("Sum copied to clipboard: $clipboardStr")
        } catch (e: NumberFormatException) {
            uiInfoService.showInfo(e.message!!)
        }
    }

    fun selectedItemClicked(position: Int, checked: Boolean) {
        treeSelectionManager.setItemSelected(position, checked)
        GUICommand().updateItemsList()
    }
}