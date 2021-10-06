package igrek.todotree.intent

import igrek.todotree.dagger.DaggerIoc
import igrek.todotree.domain.treeitem.*
import igrek.todotree.exceptions.NoSuperItemException
import igrek.todotree.info.logger.LoggerFactory.logger
import igrek.todotree.service.access.DatabaseLock
import igrek.todotree.service.history.ChangesHistory
import igrek.todotree.service.history.LinkHistoryService
import igrek.todotree.service.remote.RemotePushService
import igrek.todotree.service.remote.TodoDto
import igrek.todotree.service.resources.UserInfoService
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.service.tree.TreeMover
import igrek.todotree.service.tree.TreeScrollCache
import igrek.todotree.service.tree.TreeSelectionManager
import igrek.todotree.ui.GUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class TreeCommand {
    @Inject
    lateinit var treeManager: TreeManager

    @Inject
    lateinit var gui: GUI

    @Inject
    lateinit var lock: DatabaseLock

    @Inject
    lateinit var scrollCache: TreeScrollCache

    @Inject
    lateinit var selectionManager: TreeSelectionManager

    @Inject
    lateinit var treeMover: TreeMover

    @Inject
    lateinit var changesHistory: ChangesHistory

    @Inject
    lateinit var infoService: UserInfoService

    @Inject
    lateinit var remotePushService: RemotePushService

    @Inject
    lateinit var linkHistoryService: LinkHistoryService

    @Inject
    lateinit var userInfoService: UserInfoService

    fun goBack() {
        try {
            val current = treeManager.currentItem
            var parent = current.getParent()
            // if item was reached from link - go back to link parent
            if (linkHistoryService.hasLink(current)) {
                val linkFromTarget = linkHistoryService.getLinkFromTarget(current)
                linkHistoryService.resetTarget(current)
                parent = linkFromTarget.getParent()
                treeManager.goTo(parent)
            } else {
                treeManager.goUp()
                linkHistoryService.resetTarget(current) // reset link target - just in case
            }
            GUICommand().updateItemsList()
            GUICommand().restoreScrollPosition(parent)
        } catch (e: NoSuperItemException) {
            ExitCommand().saveAndExitRequested()
        }
    }

    fun goUp() {
        try {
            val current = treeManager.currentItem
            val parent = current.getParent()
            treeManager.goUp()
            linkHistoryService.resetTarget(current) // reset link target - just in case
            GUICommand().updateItemsList()
            GUICommand().restoreScrollPosition(parent)
        } catch (e: NoSuperItemException) {
        }
    }

    fun itemGoIntoClicked(position: Int, item: AbstractTreeItem?) {
        lock.unlockIfLocked(item)
        when (item) {
            is LinkTreeItem -> {
                goToLinkTarget(item)
            }
            is RemoteTreeItem -> {
                selectionManager.cancelSelectionMode()
                goInto(position)

                runBlocking {
                    GlobalScope.launch(Dispatchers.Main) {
                        val deferred = remotePushService.populateRemoteItemAsync((item as RemoteTreeItem?)!!)
                        val result = deferred.await()
                        result.fold(onSuccess = { todoDtos: List<TodoDto> ->
                            GUICommand().updateItemsList()
                            if (todoDtos.isEmpty()) {
                                userInfoService.showInfo("No remote items")
                            } else {
                                userInfoService.showInfo("${todoDtos.size} remote items fetched.")
                            }
                        }, onFailure = { e ->
                            logger.error(e)
                            userInfoService.showInfo("Communication breakdown!")
                        })
                    }
                }

                GUICommand().updateItemsList()
                gui.scrollToItem(0)
            }
            else -> {
                selectionManager.cancelSelectionMode()
                goInto(position)
                GUICommand().updateItemsList()
                gui.scrollToItem(0)
            }
        }
    }

    fun goInto(childIndex: Int) {
        storeCurrentScroll()
        treeManager.goInto(childIndex)
    }

    private fun navigateTo(item: AbstractTreeItem?) {
        storeCurrentScroll()
        treeManager.goTo(item)
        GUICommand().updateItemsList()
        gui.scrollToItem(0)
    }

    fun navigateToRoot() {
        navigateTo(treeManager.rootItem)
    }

    private fun storeCurrentScroll() {
        val scrollPos = gui.currentScrollPos
        if (scrollPos != null) {
            scrollCache.storeScrollPosition(treeManager.currentItem, scrollPos)
        }
    }

    fun itemLongClicked(position: Int) {
        if (!selectionManager.isAnythingSelected) {
            selectionManager.startSelectionMode()
            selectionManager.setItemSelected(position, true)
            GUICommand().updateItemsList()
            gui.scrollToItem(position)
        } else {
            selectionManager.setItemSelected(position, true)
            GUICommand().updateItemsList()
        }
    }

    fun itemClicked(position: Int, item: AbstractTreeItem) {
        lock.assertUnlocked()
        if (selectionManager.isAnythingSelected) {
            selectionManager.toggleItemSelected(position)
            GUICommand().updateItemsList()
        } else {
            if (item is RemoteTreeItem) {
                itemGoIntoClicked(position, item)
            } else if (item is TextTreeItem) {
                when {
                    !item.isEmpty -> {
                        itemGoIntoClicked(position, item)
                    }
                    item.displayName == "Tmp" && (item.getParent() == null || item.getParent() is RootTreeItem) -> {
                        itemGoIntoClicked(position, item)
                    }
                    else -> {
                        ItemEditorCommand().itemEditClicked(item)
                    }
                }
            } else if (item is LinkTreeItem) {
                goToLinkTarget(item)
            }
        }
    }

    private fun goToLinkTarget(item: LinkTreeItem) {
        // go into target
        val target = item.target
        if (target == null) {
            infoService.showInfo("Link is broken: " + item.displayTargetPath)
        } else {
            linkHistoryService.storeTargetLink(target, item)
            navigateTo(target)
        }
    }

    fun itemMoved(position: Int, step: Int): List<AbstractTreeItem> {
        treeMover.move(treeManager.currentItem, position, step)
        changesHistory.registerChange()
        return treeManager.currentItem.getChildren()
    }

    fun findItemByPath(paths: Array<String>): AbstractTreeItem? {
        var current = treeManager.rootItem
        for (path in paths) {
            val found = current.findChildByName(path) ?: return null
            current = found
        }
        return current
    }

    init {
        DaggerIoc.factoryComponent.inject(this)
    }
}