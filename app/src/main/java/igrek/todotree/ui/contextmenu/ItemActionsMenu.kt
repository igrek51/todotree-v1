package igrek.todotree.ui.contextmenu

import android.app.Activity
import android.app.AlertDialog
import igrek.todotree.dagger.DaggerIoc
import igrek.todotree.domain.treeitem.LinkTreeItem
import igrek.todotree.domain.treeitem.RemoteTreeItem
import igrek.todotree.intent.ItemActionCommand
import igrek.todotree.intent.RemotePushCommand
import igrek.todotree.service.clipboard.TreeClipboardManager
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.service.tree.TreeSelectionManager
import igrek.todotree.ui.errorcheck.UIErrorHandler
import java.util.*
import javax.inject.Inject

class ItemActionsMenu(private val position: Int) {

    @Inject
    lateinit var activity: Activity

    @Inject
    lateinit var selectionManager: TreeSelectionManager

    @Inject
    lateinit var treeManager: TreeManager

    @Inject
    lateinit var treeClipboardManager: TreeClipboardManager

    fun show() {
        val actions = filterVisibleOnly(buildActionsList())
        val actionNames = convertToNamesArray(actions)
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Choose action")
        builder.setItems(actionNames) { _, item ->
            try {
                actions[item].execute()
            } catch (t: Throwable) {
                UIErrorHandler.showError(t)
            }
        }
        val alert = builder.create()
        alert.show()
    }

    private fun buildActionsList(): List<ItemAction> {
        val actions: MutableList<ItemAction> = ArrayList()
        actions.add(object : ItemAction("Remove") {
            override fun execute() {
                ItemActionCommand().actionRemove(position)
            }

            override fun isVisible(): Boolean {
                return treeManager.isPositionAtItem(position) && treeManager.currentItem !is RemoteTreeItem
            }
        })
        actions.add(object : ItemAction("Remove from remote") {
            override fun execute() {
                ItemActionCommand().actionRemoveRemote(position)
            }

            override fun isVisible(): Boolean {
                return treeManager.currentItem is RemoteTreeItem
            }
        })
        actions.add(object : ItemAction("Remove link and target") {
            override fun execute() {
                ItemActionCommand().actionRemoveLinkAndTarget(position)
            }

            override fun isVisible(): Boolean {
                return isItemLink(position)
            }
        })
        actions.add(object : ItemAction("Select") {
            override fun execute() {
                ItemActionCommand().actionSelect(position)
            }

            override fun isVisible(): Boolean {
                return treeManager.isPositionAtItem(position)
            }
        })
        actions.add(object : ItemAction("Select all") {
            override fun execute() {
                ItemActionCommand().actionSelectAll()
            }
        })
        actions.add(object : ItemAction("Edit") {
            override fun execute() {
                ItemActionCommand().actionEdit(position)
            }

            override fun isVisible(): Boolean {
                return treeManager.isPositionAtItem(position)
            }
        })
        actions.add(object : ItemAction("Add above") {
            override fun execute() {
                ItemActionCommand().actionAddAbove(position)
            }
        })
        actions.add(object : ItemAction("Cut") {
            override fun execute() {
                ItemActionCommand().actionCut(position)
            }

            override fun isVisible(): Boolean {
                return treeManager.isPositionAtItem(position)
            }
        })
        actions.add(object : ItemAction("Copy") {
            override fun execute() {
                ItemActionCommand().actionCopy(position)
            }

            override fun isVisible(): Boolean {
                return treeManager.isPositionAtItem(position)
            }
        })
        actions.add(object : ItemAction("Paste above") {
            override fun execute() {
                ItemActionCommand().actionPasteAbove(position)
            }
        })
        actions.add(object : ItemAction("Paste as link") {
            override fun execute() {
                ItemActionCommand().actionPasteAboveAsLink(position)
            }

            override fun isVisible(): Boolean {
                return !treeClipboardManager.isClipboardEmpty
            }
        })
        actions.add(object : ItemAction("Push to remote") {
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
            if (action.isVisible) {
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

    init {
        DaggerIoc.factoryComponent.inject(this)
    }
}