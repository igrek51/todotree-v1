package igrek.todotree.intent

import igrek.todotree.dagger.FactoryComponent.inject
import igrek.todotree.domain.treeitem.AbstractTreeItem.size
import igrek.todotree.intent.GUICommand.updateItemsList
import igrek.todotree.service.resources.UserInfoService.showInfo
import javax.inject.Inject
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.service.resources.UserInfoService
import igrek.todotree.service.clipboard.SystemClipboardManager
import igrek.todotree.service.tree.TreeSelectionManager
import igrek.todotree.intent.GUICommand
import igrek.todotree.service.calc.NumericAdder
import java.lang.NumberFormatException
import igrek.todotree.dagger.DaggerIoc

class ItemSelectionCommand {
    @JvmField
	@Inject
    var treeManager: TreeManager? = null

    @JvmField
	@Inject
    var userInfo: UserInfoService? = null

    @JvmField
	@Inject
    var clipboardManager: SystemClipboardManager? = null

    @JvmField
	@Inject
    var selectionManager: TreeSelectionManager? = null
    private fun selectAllItems(selectedState: Boolean) {
        for (i in 0 until treeManager!!.currentItem.size()) {
            selectionManager!!.setItemSelected(i, selectedState)
        }
        GUICommand().updateItemsList()
    }

    fun toggleSelectAll() {
        if (selectionManager!!.selectedItemsCount == treeManager!!.currentItem.size()) {
            deselectAll()
        } else {
            selectAllItems(true)
        }
    }

    fun deselectAll() {
        selectionManager!!.cancelSelectionMode()
        GUICommand().updateItemsList()
    }

    fun sumItems() {
        val itemIds: Set<Int>
        itemIds = if (selectionManager!!.isAnythingSelected) {
            selectionManager!!.selectedItems
        } else {
            treeManager!!.allChildrenIds
        }
        try {
            val sum = NumericAdder().calculateSum(itemIds, treeManager!!.currentItem)
            var clipboardStr = sum.toPlainString()
            clipboardStr = clipboardStr.replace('.', ',')
            clipboardManager!!.copyToSystemClipboard(clipboardStr)
            userInfo!!.showInfo("Sum copied to clipboard: $clipboardStr")
        } catch (e: NumberFormatException) {
            userInfo!!.showInfo(e.message!!)
        }
    }

    fun selectedItemClicked(position: Int, checked: Boolean) {
        selectionManager!!.setItemSelected(position, checked)
        GUICommand().updateItemsList()
    }

    init {
        DaggerIoc.factoryComponent.inject(this)
    }
}