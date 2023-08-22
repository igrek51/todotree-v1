package igrek.todotree.ui.contextmenu

import android.app.Activity
import android.app.AlertDialog
import igrek.todotree.domain.treeitem.LinkTreeItem
import igrek.todotree.domain.treeitem.RemoteTreeItem
import igrek.todotree.domain.treeitem.TextTreeItem
import igrek.todotree.info.errorcheck.UiErrorHandler
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.intent.ItemActionCommand
import igrek.todotree.intent.RemotePushCommand
import igrek.todotree.service.clipboard.TreeClipboardManager
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.ui.ExplosionService
import igrek.todotree.ui.SizeAndPosition

class ItemActionsMenu(
    private val position: Int,
    treeManager: LazyInject<TreeManager> = appFactory.treeManager,
    treeClipboardManager: LazyInject<TreeClipboardManager> = appFactory.treeClipboardManager,
    explosionService: LazyInject<ExplosionService> = appFactory.explosionService,
) {
    private val activity: Activity by LazyExtractor(appFactory.activity)
    private val treeManager by LazyExtractor(treeManager)
    private val treeClipboardManager by LazyExtractor(treeClipboardManager)
    private val explosionService by LazyExtractor(explosionService)

    fun show(coordinates: SizeAndPosition?) {
        val actions = filterVisibleOnly(buildActionsList(coordinates))
        val actionNames = convertToNamesArray(actions)
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Choose action")
        builder.setItems(actionNames) { _, item ->
            try {
                actions[item].execute()
            } catch (t: Throwable) {
                UiErrorHandler.handleError(t)
            }
        }
        val alert = builder.create()
        alert.show()
    }

    private fun buildActionsList(coordinates: SizeAndPosition?): List<ItemAction> {
        val actions: MutableList<ItemAction> = ArrayList()
        actions.add(object : ItemAction("❌ Remove") {
            override fun execute() {
                explosionService.explode(coordinates)
                ItemActionCommand().actionRemove(position)
            }

            override fun isVisible(): Boolean {
                return treeManager.isPositionAtItem(position) && treeManager.currentItem !is RemoteTreeItem
            }
        })
        actions.add(object : ItemAction("❌ Remove from remote") {
            override fun execute() {
                explosionService.explode(coordinates)
                ItemActionCommand().actionRemoveRemote(position)
            }

            override fun isVisible(): Boolean {
                return treeManager.currentItem is RemoteTreeItem
            }
        })
        actions.add(object : ItemAction("\uD83D\uDDD1️ Remove link and target") {
            override fun execute() {
                explosionService.explode(coordinates)
                ItemActionCommand().actionRemoveLinkAndTarget(position)
            }

            override fun isVisible(): Boolean {
                return isItemLink(position)
            }
        })
        actions.add(object : ItemAction("✔️ Select") {
            override fun execute() {
                ItemActionCommand().actionSelect(position)
            }

            override fun isVisible(): Boolean {
                return treeManager.isPositionAtItem(position)
            }
        })
        actions.add(object : ItemAction("☑️ Select all") {
            override fun execute() {
                ItemActionCommand().actionSelectAll()
            }
        })
        actions.add(object : ItemAction("✏️ Edit") {
            override fun execute() {
                ItemActionCommand().actionEdit(position)
            }

            override fun isVisible(): Boolean {
                return treeManager.isPositionAtItem(position)
            }
        })
        actions.add(object : ItemAction("➕ Add above") {
            override fun execute() {
                ItemActionCommand().actionAddAbove(position)
            }
        })
        actions.add(object : ItemAction("✂️ Cut") {
            override fun execute() {
                ItemActionCommand().actionCut(position)
            }

            override fun isVisible(): Boolean {
                return treeManager.isPositionAtItem(position)
            }
        })
        actions.add(object : ItemAction("\uD83D\uDCC4 Copy") {
            override fun execute() {
                ItemActionCommand().actionCopy(position)
            }

            override fun isVisible(): Boolean {
                return treeManager.isPositionAtItem(position)
            }
        })
        actions.add(object : ItemAction("\uD83D\uDCCB Paste above") {
            override fun execute() {
                ItemActionCommand().actionPasteAbove(position)
            }
        })
        actions.add(object : ItemAction("\uD83D\uDD17 Paste as link") {
            override fun execute() {
                ItemActionCommand().actionPasteAboveAsLink(position)
            }

            override fun isVisible(): Boolean {
                return !treeClipboardManager.isClipboardEmpty
            }
        })
        actions.add(object : ItemAction("\uD83D\uDD2A Split") {
            override fun execute() {
                ItemActionCommand().actionSplit(position)
            }

            override fun isVisible(): Boolean {
                return treeManager.isPositionAtItem(position)
                        && treeManager.currentItem?.getChild(position) is TextTreeItem
                        && treeManager.currentItem?.getChild(position)?.displayName?.contains(",") == true
            }
        })
        actions.add(object : ItemAction("\uD83D\uDCE4 Push to remote") {
            override fun execute() {
                RemotePushCommand().actionPushItemsToRemote(position)
            }
        })
        return actions
    }

    private fun isItemLink(position: Int): Boolean {
        if (!treeManager.isPositionAtItem(position)) return false
        val item = treeManager.getChild(position)
        return item is LinkTreeItem
    }

    private fun filterVisibleOnly(actions: List<ItemAction>): List<ItemAction> {
        val visibleActions: MutableList<ItemAction> = ArrayList()
        for (action in actions) {
            if (action.isVisible()) {
                visibleActions.add(action)
            }
        }
        return visibleActions
    }

    private fun convertToNamesArray(actions: List<ItemAction>): Array<CharSequence?> {
        val actionNames = arrayOfNulls<CharSequence>(actions.size)
        for (i in actions.indices) {
            actionNames[i] = actions[i].name
        }
        return actionNames
    }
}