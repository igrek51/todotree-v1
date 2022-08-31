package igrek.todotree.service.tree.persistence

import igrek.todotree.domain.treeitem.AbstractTreeItem.typeName
import igrek.todotree.domain.treeitem.AbstractTreeItem.isEmpty
import igrek.todotree.domain.treeitem.AbstractTreeItem.getChildren
import igrek.todotree.domain.treeitem.AbstractTreeItem.displayName
import igrek.todotree.domain.treeitem.LinkTreeItem.targetPath
import igrek.todotree.domain.treeitem.LinkTreeItem.hasCustomName
import igrek.todotree.domain.treeitem.LinkTreeItem.customName
import igrek.todotree.domain.treeitem.CheckboxTreeItem.isChecked
import igrek.todotree.domain.treeitem.AbstractTreeItem
import java.lang.StringBuilder
import igrek.todotree.domain.treeitem.RemoteTreeItem
import igrek.todotree.domain.treeitem.TextTreeItem
import igrek.todotree.domain.treeitem.LinkTreeItem
import igrek.todotree.domain.treeitem.CheckboxTreeItem

internal class JsonTreeSerializer {
    fun serializeTree(root: AbstractTreeItem): String {
        val output = StringBuilder()
        serializeItem(output, root, 0)
        return output.toString()
    }

    private fun serializeItem(output: StringBuilder, item: AbstractTreeItem, indentLevel: Int) {
        indent(output, indentLevel)
        // item type
        output.append("{ \"type\": \"")
        output.append(item.typeName)
        output.append("\"")
        // additional attributes
        serializeAttributes(output, item)
        // child items
        if (item.isEmpty) {
            output.append(" },\n")
        } else {
            output.append(", \"items\": [\n")
            for (child in item.getChildren()) {
                serializeItem(output, child, indentLevel + 1)
            }
            // end of children list
            indent(output, indentLevel)
            output.append("]},\n")
        }
    }

    private fun indent(output: StringBuilder, indentLevel: Int) {
        for (i in 0 until indentLevel) output.append("\t")
    }

    private fun serializeAttributes(output: StringBuilder, item: AbstractTreeItem) {
        if (item is RemoteTreeItem) {
            serializeAttribute(output, "name", item.displayName)
        } else if (item is TextTreeItem) {
            serializeAttribute(output, "name", item.displayName)
        } else if (item is LinkTreeItem) {
            val linkItem = item
            serializeAttribute(output, "target", linkItem.targetPath)
            if (linkItem.hasCustomName()) {
                serializeAttribute(output, "name", linkItem.customName)
            }
        } else if (item is CheckboxTreeItem) {
            serializeAttribute(output, "checked", if (item.isChecked) "true" else "false")
        }
    }

    private fun serializeAttribute(output: StringBuilder, name: String, value: String?) {
        output.append(", \"")
        output.append(name)
        output.append("\": \"")
        output.append(escape(value))
        output.append("\"")
    }

    private fun escape(s: String?): String {
        var s = s
        s = s!!.replace("\\", "\\\\") // escape \
        s = s.replace("\"", "\\\"") // escape "
        return s
    }
}