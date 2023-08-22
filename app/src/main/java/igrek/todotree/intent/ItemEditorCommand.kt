package igrek.todotree.intent

import igrek.todotree.app.AppData
import igrek.todotree.app.AppState
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.domain.treeitem.LinkTreeItem
import igrek.todotree.domain.treeitem.RemoteTreeItem
import igrek.todotree.domain.treeitem.TextTreeItem
import igrek.todotree.info.Toaster
import igrek.todotree.info.UiInfoService
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.service.access.QuickAddService
import igrek.todotree.service.history.ChangesHistory
import igrek.todotree.service.remote.RemotePushService
import igrek.todotree.service.tree.ContentTrimmer
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.service.tree.TreeScrollCache
import igrek.todotree.service.tree.TreeSelectionManager
import igrek.todotree.ui.GUI
import kotlinx.coroutines.*

@OptIn(DelicateCoroutinesApi::class)
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
    remotePushService: LazyInject<RemotePushService> = appFactory.remotePushService,
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
    private val remotePushService by LazyExtractor(remotePushService)

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
            treeManager.addToCurrent(newItemPosition, TextTreeItem(content))
            uiInfoService.showInfo("New item saved: $content")
            true
        }
    }

    fun createRemoteItem() {
        treeManager.addToCurrent(null, RemoteTreeItem("Remote"))
    }

    private fun tryToSaveExistingItem(editedItem: TextTreeItem, _content: String): Boolean {
        var content = _content
        content = contentTrimmer.trimContent(content)
        return if (content.isEmpty()) {
            treeManager.removeFromCurrent(editedItem)
            uiInfoService.showInfo("Empty item has been removed.")
            false
        } else {
            editedItem.setName(content)
            changesHistory.registerChange()
            uiInfoService.showInfo("Item saved.")
            true
        }
    }

    private fun tryToSaveExistingLink(editedLinkItem: LinkTreeItem, _content: String): Boolean {
        var content = _content
        content = contentTrimmer.trimContent(content)
        return if (content.isEmpty()) {
            treeManager.removeFromCurrent(editedLinkItem)
            uiInfoService.showInfo("Empty link has been removed.")
            false
        } else {
            editedLinkItem.customName = content
            changesHistory.registerChange()
            uiInfoService.showInfo("Link name has been saved.")
            true
        }
    }

    private fun tryToSaveItem(editedItem: AbstractTreeItem?, content: String): Boolean {
        return if (editedItem == null) { // new item
            tryToSaveNewItem(content)
        } else { // existing item
            when (editedItem) {
                is TextTreeItem -> {
                    tryToSaveExistingItem(editedItem, content)
                }
                is LinkTreeItem -> {
                    tryToSaveExistingLink(editedItem, content)
                }
                else -> {
                    logger.warn("trying to save item of type: " + editedItem.typeName)
                    false
                }
            }
        }
    }

    private fun returnFromItemEditing() {
        GUICommand().showItemsList()
        // when new item has been added to the end
        if (newItemPosition != null
            && newItemPosition == (treeManager.currentItem?.size() ?: 0) - 1
        ) {
//            gui.scrollToBottom()
        } else {
//            GUICommand().restoreScrollPosition(treeManager.currentItem)
        }
        treeManager.newItemPosition = null
    }

    fun saveItem(editedItem: AbstractTreeItem?, content: String) {
        tryToSaveItem(editedItem, content)
        // try to execute secret command
        returnFromItemEditing()
        // exit if it's quick add mode only
        if (quickAddService.isQuickAddModeEnabled) {
            quickAddService.exitApp()
        } else if (remotePushService.isRemotePushEnabled) {
            runBlocking {
                GlobalScope.launch(Dispatchers.Main) {
                    val result = remotePushService.pushAndExitAsync(content).await()
                    result.fold(onSuccess = {
                        uiInfoService.showToast("Success!")
                        exitApp()
                    }, onFailure = { e ->
                        Toaster().error(e, "Communication breakdown!")
                    })
                }
            }
        }
    }

    private fun exitApp() {
        logger.debug("Exitting quick add mode...")
        ExitCommand().optionSaveAndExit()
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
        treeScrollCache.storeScrollPosition(treeManager.currentItem, gui.currentScrollPos)
        treeManager.newItemPosition = position
        treeManager.currentItem?.let { currentItem ->
            gui.showEditItemPanel(null, currentItem)
        }
        appData.state = AppState.EDIT_ITEM_CONTENT
    }

    private fun editItem(item: AbstractTreeItem, parent: AbstractTreeItem) {
        treeScrollCache.storeScrollPosition(treeManager.currentItem, gui.currentScrollPos)
        treeManager.newItemPosition = null
        gui.showEditItemPanel(item, parent)
        appData.state = AppState.EDIT_ITEM_CONTENT
    }

    private fun discardEditingItem() {
        returnFromItemEditing()
        uiInfoService.showInfo("Editing item cancelled.")
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
        } else if (remotePushService.isRemotePushEnabled) {
            exitApp()
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