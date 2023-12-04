package igrek.todotree.intent

import igrek.todotree.app.AppData
import igrek.todotree.app.AppState
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.domain.treeitem.LinkTreeItem
import igrek.todotree.domain.treeitem.RemoteTreeItem
import igrek.todotree.domain.treeitem.TextTreeItem
import igrek.todotree.info.UiInfoService
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.service.access.QuickAddService
import igrek.todotree.service.history.ChangesHistory
import igrek.todotree.service.tree.ContentTrimmer
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.service.tree.TreeScrollCache
import igrek.todotree.service.tree.TreeSelectionManager
import igrek.todotree.ui.GUI

class ItemEditorCommand(
    treeManager: LazyInject<TreeManager> = appFactory.treeManager,
    gui: LazyInject<GUI> = appFactory.gui,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    appData: LazyInject<AppData> = appFactory.appData,
    contentTrimmer: LazyInject<ContentTrimmer> = appFactory.contentTrimmer,
    treeScrollCache: LazyInject<TreeScrollCache> = appFactory.treeScrollCache,
    treeSelectionManager: LazyInject<TreeSelectionManager> = appFactory.treeSelectionManager,
    changesHistory: LazyInject<ChangesHistory> = appFactory.changesHistory,
    quickAddService: LazyInject<QuickAddService> = appFactory.quickAddService,
) {
    private val treeManager by LazyExtractor(treeManager)
    private val gui by LazyExtractor(gui)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val appData by LazyExtractor(appData)
    private val contentTrimmer by LazyExtractor(contentTrimmer)
    private val treeScrollCache by LazyExtractor(treeScrollCache)
    private val treeSelectionManager by LazyExtractor(treeSelectionManager)
    private val changesHistory by LazyExtractor(changesHistory)
    private val quickAddService by LazyExtractor(quickAddService)

    private val logger = LoggerFactory.logger
    private val newItemPosition: Int?
        get() = treeManager.newItemPosition

    private fun tryToSaveNewItem(aContent: String): Boolean {
        var content = aContent
        content = contentTrimmer.trimContent(content)
        return if (content.isEmpty()) {
            uiInfoService.showInfo("Empty item has been removed.")
            false
        } else {
            val newItem = TextTreeItem(content)
            treeManager.addToCurrent(newItemPosition, newItem)
            uiInfoService.showInfo("New item saved: $content")
            true
        }
    }

    fun createRemoteItem() {
        treeManager.addToCurrent(null, RemoteTreeItem("Remote"))
    }

    private fun tryToSaveExistingItem(editedItem: AbstractTreeItem, content: String): Boolean {
        var mContent = content
        mContent = contentTrimmer.trimContent(mContent)
        return if (mContent.isEmpty()) {
            treeManager.removeFromCurrent(editedItem)
            uiInfoService.showInfo("Empty item has been removed.")
            false
        } else {
            when (editedItem) {
                is TextTreeItem -> {
                    editedItem.setName(mContent)
                }
                is RemoteTreeItem -> {
                    editedItem.setName(mContent)
                }
                is LinkTreeItem -> {
                    editedItem.customName = mContent
                }
            }
            treeManager.focusItem = editedItem
            changesHistory.registerChange()
            true
        }
    }

    private fun tryToSaveItem(editedItem: AbstractTreeItem?, content: String): Boolean {
        return if (editedItem == null) { // new item
            tryToSaveNewItem(content)
        } else { // existing item
            when (editedItem) {
                is TextTreeItem, is RemoteTreeItem, is LinkTreeItem -> {
                    tryToSaveExistingItem(editedItem, content)
                }
                else -> {
                    logger.warn("trying to save item of type: " + editedItem.typeName)
                    false
                }
            }
        }
    }

    private fun returnFromItemEditing() {
        gui.showItemsList()
        // when new item has been added to the end
        if (newItemPosition != null
            && newItemPosition == (treeManager.currentItem?.size() ?: 0) - 1
        ) {
            treeScrollCache.scrollToBottom()
        } else {
            treeScrollCache.restoreScrollPosition()
        }
        treeManager.newItemPosition = null
    }

    fun saveItem(editedItem: AbstractTreeItem?, content: String) {
        treeManager.focusItem = editedItem
        tryToSaveItem(editedItem, content)
        // try to execute secret command
        returnFromItemEditing()
        // exit if it's quick add mode only
        if (quickAddService.isQuickAddModeEnabled) {
            quickAddService.exitApp()
        }
    }

    private fun exitApp() {
        logger.debug("Exitting quick add mode...")
        ExitCommand().saveItemAndExit()
    }

    fun saveAndAddItemClicked(editedItem: AbstractTreeItem?, content: String) {
        val newItemIndex = editedItem?.indexInParent ?: newItemPosition!!
        if (!tryToSaveItem(editedItem, content)) {
            returnFromItemEditing()
            return
        }
        // add new after
        newItem(newItemIndex + 1)
    }

    fun saveAndGoIntoItemClicked(editedItem: AbstractTreeItem?, content: String) {
        if (!tryToSaveItem(editedItem, content)) {
            returnFromItemEditing()
            return
        }
        // go into
        var editedItemIndex = newItemPosition
        if (newItemPosition == null) {
            editedItemIndex = editedItem?.indexInParent
        }
        editedItemIndex?.let {
            TreeCommand().goInto(editedItemIndex)
            newItem(-1)
        }
    }

    /**
     * @param _position posistion of new element (0 - begginning, negative value - in the end of list)
     */
    private fun newItem(_position: Int) {
        var position = _position
        if (position < 0) position = treeManager.currentItem?.size() ?: 0
        if (position > (treeManager.currentItem?.size() ?: 0))
            position = treeManager.currentItem?.size() ?: 0
        treeScrollCache.storeScrollPosition()
        treeManager.newItemPosition = position
        treeManager.currentItem?.let { currentItem ->
            gui.showEditItemPanel(null, currentItem)
        }
        appData.state = AppState.EDIT_ITEM_CONTENT
    }

    private fun editItem(item: AbstractTreeItem, parent: AbstractTreeItem) {
        treeScrollCache.storeScrollPosition()
        treeManager.newItemPosition = null
        treeManager.focusItem = item
        gui.showEditItemPanel(item, parent)
        appData.state = AppState.EDIT_ITEM_CONTENT
    }

    private fun discardEditingItem() {
        returnFromItemEditing()
    }

    fun itemEditClicked(item: AbstractTreeItem) {
        treeSelectionManager.cancelSelectionMode()
        treeManager.currentItem?.let { currentItem ->
            editItem(item, currentItem)
        }
    }

    fun cancelEditedItem() {
        gui.hideSoftKeyboard()
        discardEditingItem()
        // exit if it's quick add mode only
        if (quickAddService.isQuickAddModeEnabled) {
            quickAddService.exitApp()
        }
    }

    fun addItemHereClicked(position: Int) {
        treeSelectionManager.cancelSelectionMode()
        newItem(position)
    }

    fun addItemClicked() {
        addItemHereClicked(-1)
    }
}