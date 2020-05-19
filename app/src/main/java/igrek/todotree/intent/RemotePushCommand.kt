package igrek.todotree.intent

import igrek.todotree.dagger.DaggerIoc
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.service.remote.RemotePushService
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.service.tree.TreeSelectionManager
import java.util.*
import javax.inject.Inject

class RemotePushCommand {
    @Inject
    lateinit var remotePushService: RemotePushService
    @Inject
    lateinit var selectionManager: TreeSelectionManager
    @Inject
    lateinit var treeManager: TreeManager

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun isRemotePushingEnabled(): Boolean {
        return remotePushService.isRemotePushingEnabled
    }

    fun actionPushItemsToRemote(position: Int) {
        val itemPosistions: TreeSet<Int> = TreeSet(selectionManager.selectedItemsNotNull)
        if (itemPosistions.isEmpty()) {
            itemPosistions.add(position)
        }

        if (!itemPosistions.isEmpty()) {
            val currentItem: AbstractTreeItem = treeManager.currentItem
            for (selectedItemId in itemPosistions) {
                val selectedItem = currentItem.getChild(selectedItemId)
                selectedItem.displayName
                remotePushService.pushNewItem(selectedItem.displayName)
            }
        }
    }


}