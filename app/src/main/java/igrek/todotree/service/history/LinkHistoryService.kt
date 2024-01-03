package igrek.todotree.service.history

import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.domain.treeitem.LinkTreeItem
import java.util.HashMap

class LinkHistoryService {

    private val target2link: MutableMap<AbstractTreeItem, LinkTreeItem> = HashMap()

    fun storeTargetLink(target: AbstractTreeItem, link: LinkTreeItem) {
        target2link[target] = link
    }

    fun getLinkFromTarget(target: AbstractTreeItem): LinkTreeItem? {
        return target2link[target]
    }

    fun hasLink(target: AbstractTreeItem): Boolean {
        return target2link.containsKey(target)
    }

    fun resetTarget(target: AbstractTreeItem) {
        target2link.remove(target)
    }

    fun clear() {
        target2link.clear()
    }
}