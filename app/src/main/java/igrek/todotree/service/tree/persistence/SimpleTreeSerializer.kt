package igrek.todotree.service.tree.persistence

import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.domain.treeitem.RootTreeItem
import igrek.todotree.domain.treeitem.TextTreeItem
import java.text.ParseException

object SimpleTreeSerializer {

    @Throws(ParseException::class)
    fun loadTree(data: String): AbstractTreeItem {
        val rootItem = RootTreeItem()
        if (!data.isEmpty()) {
            //wyłuskanie wierszy
            val lines = data.split("\n").toTypedArray()
            val linesList: MutableList<String> = ArrayList()
            //obcięcie białych znaków
            for (line in lines) {
                val trimmed = line.trim { it <= ' ' }
                if (!trimmed.isEmpty()) {
                    linesList.add(trimmed)
                }
            }
            loadTreeItems(rootItem, linesList)
        }
        return rootItem
    }

    @Throws(ParseException::class)
    private fun loadTreeItems(parent: AbstractTreeItem, lines: List<String>) {
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            when (line) {
                "{" -> i = try {
                    val closingBracketIndex = findClosingBracket(lines, i)
                    //jeśli cokolwiek jest w środku bloku
                    if (closingBracketIndex - i >= 2) {
                        val subLines = lines.subList(i + 1, closingBracketIndex)
                        val lastChild = parent.lastChild
                            ?: throw ParseException("No matching element before opening bracket", i)
                        loadTreeItems(lastChild, subLines)
                    }
                    //przeskoczenie już przeanalizowanych wierszy
                    closingBracketIndex
                } catch (ex: RuntimeException) {
                    throw ParseException("No matching closing bracket", i)
                }
                "}" -> throw ParseException("Redundant closing bracket", i)
                else -> parent.add(TextTreeItem(parent, line))
            }
            i++
        }
    }

    private fun findClosingBracket(lines: List<String>, startIndex: Int): Int {
        var bracketDepth = 1
        for (j in startIndex + 1 until lines.size) {
            val line = lines[j]
            if (line == "{") {
                bracketDepth++
            } else if (line == "}") {
                bracketDepth--
                if (bracketDepth == 0) {
                    return j
                }
            }
        }
        throw RuntimeException("NoMatchingBracket")
    }
}