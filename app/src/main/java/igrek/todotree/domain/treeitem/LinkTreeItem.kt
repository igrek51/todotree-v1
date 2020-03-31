package igrek.todotree.domain.treeitem

import com.google.common.base.Joiner
import igrek.todotree.intent.TreeCommand

class LinkTreeItem(
        parent: AbstractTreeItem?,
        var targetPath: String,
        var customName: String?,
) : AbstractTreeItem(parent) {

    override fun clone(): LinkTreeItem {
        return LinkTreeItem(null, targetPath, customName).copyChildren(this)
    }

    override val displayName: String
        get() {
            if (hasCustomName())
                return customName.orEmpty()
            val target = target
            return target?.displayName ?: displayTargetPath
        }

    override val typeName: String
        get() = "link"

    fun hasCustomName(): Boolean {
        return customName != null
    }

    val displayTargetPath: String
        get() = "/" + targetPath.replace("\t", "/")

    val target: AbstractTreeItem?
        get() {
            val paths = targetPath.split("\\t").toTypedArray()
            return TreeCommand().findItemByPath(paths)
        }

    fun setTarget(target: AbstractTreeItem) {
        val joiner = Joiner.on("\t")
        val names = target.namesPaths
        targetPath = joiner.join(names)
    }

    fun setTarget(targetParent: AbstractTreeItem, targetName: String?) {
        val joiner = Joiner.on("\t")
        val names = targetParent.namesPaths
        names.add(targetName)
        targetPath = joiner.join(names)
    }

    val isBroken: Boolean
        get() = target == null

}