package igrek.todotree.intent

import igrek.todotree.app.AppData
import igrek.todotree.app.AppState
import igrek.todotree.dagger.DaggerIoc
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.domain.treeitem.LinkTreeItem
import igrek.todotree.domain.treeitem.RemoteTreeItem
import igrek.todotree.domain.treeitem.TextTreeItem
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.service.access.QuickAddService
import igrek.todotree.service.commander.SecretCommander
import igrek.todotree.service.history.ChangesHistory
import igrek.todotree.service.remote.RemotePushService
import igrek.todotree.service.resources.UserInfoService
import igrek.todotree.service.tree.ContentTrimmer
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.service.tree.TreeScrollCache
import igrek.todotree.service.tree.TreeSelectionManager
import igrek.todotree.ui.GUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class ItemEditorCommand {
    @Inject
    lateinit var treeManager: TreeManager

    @Inject
    lateinit var gui: GUI

    @Inject
    lateinit var userInfo: UserInfoService

    @Inject
    lateinit var contentTrimmer: ContentTrimmer

    @Inject
    lateinit var appData: AppData

    @Inject
    lateinit var scrollCache: TreeScrollCache

    @Inject
    lateinit var selectionManager: TreeSelectionManager

    @Inject
    lateinit var changesHistory: ChangesHistory

    @Inject
    lateinit var secretCommander: SecretCommander

    @Inject
    lateinit var quickAddService: QuickAddService

    @Inject
    lateinit var remotePushService: RemotePushService

    @Inject
    lateinit var userInfoService: UserInfoService

    private val logger = LoggerFactory.logger
    private val newItemPosition: Int?
        get() = treeManager.newItemPosition

    private fun tryToSaveNewItem(_content: String): Boolean {
        var content = _content
        content = contentTrimmer.trimContent(content)
        return if (content.isEmpty()) {
            userInfo.showInfo("Empty item has been removed.")
            false
        } else {
            treeManager.addToCurrent(newItemPosition, TextTreeItem(content))
            userInfo.showInfo("New item has been saved.")
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
            userInfo.showInfo("Empty item has been removed.")
            false
        } else {
            editedItem.setName(content)
            changesHistory.registerChange()
            userInfo.showInfo("Item has been saved.")
            true
        }
    }

    private fun tryToSaveExistingLink(editedLinkItem: LinkTreeItem, _content: String): Boolean {
        var content = _content
        content = contentTrimmer.trimContent(content)
        return if (content.isEmpty()) {
            treeManager.removeFromCurrent(editedLinkItem)
            userInfo.showInfo("Empty link has been removed.")
            false
        } else {
            editedLinkItem.customName = content
            changesHistory.registerChange()
            userInfo.showInfo("Link name has been saved.")
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
        if (newItemPosition != null && newItemPosition == treeManager.currentItem.size() - 1) {
            gui.scrollToBottom()
        } else {
            GUICommand().restoreScrollPosition(treeManager.currentItem)
        }
        treeManager.newItemPosition = null
    }

    fun saveItem(editedItem: AbstractTreeItem?, content: String) {
        tryToSaveItem(editedItem, content)
        // try to execute secret command
        secretCommander.execute(content)
        returnFromItemEditing()
        // exit if it's quick add mode only
        if (quickAddService.isQuickAddMode) {
            quickAddService.exitApp()
        } else if (remotePushService.isRemotePushingEnabled) {
            runBlocking {
                GlobalScope.launch(Dispatchers.Main) {
                    val result = remotePushService.pushAndExitAsync(content).await()
                    result.fold(onSuccess = {
                        userInfoService.showToast("Success!")
                        exitApp()
                    }, onFailure = { e ->
                        logger.error(e)
                        userInfoService.showToast("Communication breakdown!")
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
        if (position < 0) position = treeManager.currentItem.size()
        if (position > treeManager.currentItem.size()) position = treeManager.currentItem.size()
        scrollCache.storeScrollPosition(treeManager.currentItem, gui.currentScrollPos)
        treeManager.newItemPosition = position
        gui.showEditItemPanel(null, treeManager.currentItem)
        appData.state = AppState.EDIT_ITEM_CONTENT
    }

    private fun editItem(item: AbstractTreeItem, parent: AbstractTreeItem) {
        scrollCache.storeScrollPosition(treeManager.currentItem, gui.currentScrollPos)
        treeManager.newItemPosition = null
        gui.showEditItemPanel(item, parent)
        appData.state = AppState.EDIT_ITEM_CONTENT
    }

    private fun discardEditingItem() {
        returnFromItemEditing()
        userInfo.showInfo("Editing item cancelled.")
    }

    fun itemEditClicked(item: AbstractTreeItem) {
        selectionManager.cancelSelectionMode()
        editItem(item, treeManager.currentItem)
    }

    fun cancelEditedItem() {
        gui.hideSoftKeyboard()
        discardEditingItem()
        // exit if it's quick add mode only
        if (quickAddService.isQuickAddMode) {
            quickAddService.exitApp()
        } else if (remotePushService.isRemotePushingEnabled) {
            exitApp()
        }
    }

    fun addItemHereClicked(position: Int) {
        selectionManager.cancelSelectionMode()
        newItem(position)
    }

    fun addItemClicked() {
        addItemHereClicked(-1)
    }

    init {
        DaggerIoc.factoryComponent.inject(this)
    }
}