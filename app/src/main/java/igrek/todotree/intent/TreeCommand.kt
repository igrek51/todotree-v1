package igrek.todotree.intent

import igrek.todotree.domain.treeitem.*
import igrek.todotree.exceptions.NoSuperItemException
import igrek.todotree.info.Toaster
import igrek.todotree.info.UiInfoService
import igrek.todotree.info.logger.LoggerFactory.logger
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.service.access.DatabaseLock
import igrek.todotree.service.history.ChangesHistory
import igrek.todotree.service.history.LinkHistoryService
import igrek.todotree.service.remote.RemotePushService
import igrek.todotree.service.remote.TodoDto
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.service.tree.TreeMover
import igrek.todotree.service.tree.TreeScrollCache
import igrek.todotree.service.tree.TreeSelectionManager
import igrek.todotree.ui.GUI
import kotlinx.coroutines.*

@OptIn(DelicateCoroutinesApi::class)
class TreeCommand(
    treeManager: LazyInject<TreeManager> = appFactory.treeManager,
    gui: LazyInject<GUI> = appFactory.gui,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    databaseLock: LazyInject<DatabaseLock> = appFactory.databaseLock,
    treeScrollCache: LazyInject<TreeScrollCache> = appFactory.treeScrollCache,
    treeSelectionManager: LazyInject<TreeSelectionManager> = appFactory.treeSelectionManager,
    treeMover: LazyInject<TreeMover> = appFactory.treeMover,
    changesHistory: LazyInject<ChangesHistory> = appFactory.changesHistory,
    remotePushService: LazyInject<RemotePushService> = appFactory.remotePushService,
    linkHistoryService: LazyInject<LinkHistoryService> = appFactory.linkHistoryService,
) {
    private val treeManager by LazyExtractor(treeManager)
    private val gui by LazyExtractor(gui)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val databaseLock by LazyExtractor(databaseLock)
    private val treeScrollCache by LazyExtractor(treeScrollCache)
    private val treeSelectionManager by LazyExtractor(treeSelectionManager)
    private val treeMover by LazyExtractor(treeMover)
    private val changesHistory by LazyExtractor(changesHistory)
    private val remotePushService by LazyExtractor(remotePushService)
    private val linkHistoryService by LazyExtractor(linkHistoryService)

    fun goBack() {
        try {
            val current = treeManager.currentItem!!
            val parent = current.getParent()
            // if item was reached from link - go back to link parent
            if (linkHistoryService.hasLink(current)) {
                val linkFromTarget = linkHistoryService.getLinkFromTarget(current)
                linkHistoryService.resetTarget(current)
                linkFromTarget?.getParent()?.let { linkParent ->
                    treeManager.goTo(linkParent)
                }
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
            val current = treeManager.currentItem!!
            val parent = current.getParent()
            treeManager.goUp()
            linkHistoryService.resetTarget(current) // reset link target - just in case
            GUICommand().updateItemsList()
            GUICommand().restoreScrollPosition(parent)
        } catch (e: NoSuperItemException) {
        }
    }

    fun itemGoIntoClicked(position: Int, item: AbstractTreeItem?) {
        databaseLock.unlockIfLocked(item)
        when (item) {
            is LinkTreeItem -> {
                goToLinkTarget(item)
            }
            is RemoteTreeItem -> {
                treeSelectionManager.cancelSelectionMode()
                goInto(position)

                runBlocking {
                    GlobalScope.launch(Dispatchers.Main) {
                        val deferred = remotePushService.populateRemoteItemAsync((item as RemoteTreeItem?)!!)
                        val result = deferred.await()
                        result.fold(onSuccess = { todoDtos: List<TodoDto> ->
                            GUICommand().updateItemsList()
                            if (todoDtos.isEmpty()) {
                                uiInfoService.showInfo("No remote items")
                            } else {
                                uiInfoService.showInfo("${todoDtos.size} remote items fetched.")
                            }
                        }, onFailure = { e ->
                            Toaster().error(e, "Communication breakdown!")
                        })
                    }
                }

                GUICommand().updateItemsList()
                gui.scrollToItem(0)
            }
            else -> {
                treeSelectionManager.cancelSelectionMode()
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
            treeScrollCache.storeScrollPosition(treeManager.currentItem, scrollPos)
        }
    }

    fun itemLongClicked(position: Int) {
        if (!treeSelectionManager.isAnythingSelected) {
            treeSelectionManager.startSelectionMode()
            treeSelectionManager.setItemSelected(position, true)
            GUICommand().updateItemsList()
            gui.scrollToItem(position)
        } else {
            treeSelectionManager.setItemSelected(position, true)
            GUICommand().updateItemsList()
        }
    }

    fun itemClicked(position: Int, item: AbstractTreeItem) {
        databaseLock.assertUnlocked()
        if (treeSelectionManager.isAnythingSelected) {
            treeSelectionManager.toggleItemSelected(position)
            GUICommand().updateItemsList()
        } else {
            when (item) {
                is RemoteTreeItem -> {
                    itemGoIntoClicked(position, item)
                }
                is TextTreeItem -> {
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
                }
                is LinkTreeItem -> {
                    goToLinkTarget(item)
                }
            }
        }
    }

    private fun goToLinkTarget(item: LinkTreeItem) {
        // go into target
        val target = item.target
        if (target == null) {
            uiInfoService.showInfo("Link is broken: " + item.displayTargetPath)
        } else {
            linkHistoryService.storeTargetLink(target, item)
            navigateTo(target)
        }
    }

    fun itemMoved(position: Int, step: Int): List<AbstractTreeItem> {
        treeMover.move(treeManager.currentItem!!, position, step)
        changesHistory.registerChange()
        return treeManager.currentItem!!.children
    }

    fun findItemByPath(paths: Array<String>): AbstractTreeItem? {
        var current = treeManager.rootItem
        for (path in paths) {
            val found = current!!.findChildByName(path) ?: return null
            current = found
        }
        return current
    }
}