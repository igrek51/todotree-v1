package igrek.todotree.service.tree.persistence

import igrek.todotree.domain.treeitem.*
import igrek.todotree.exceptions.DeserializationFailedException
import java.util.regex.Pattern

internal class JsonTreeDeserializer {

    private val singleItemPattern: Pattern
    private val multiItemPattern: Pattern
    private val nameValuePattern: Pattern
    private val BLOCK_CLOSING_BRACKET = "]},"

    @Throws(DeserializationFailedException::class)
    fun deserializeTree(data: String): AbstractTreeItem {
        val rootItem = RootTreeItem()
        if (data.isEmpty()) throw DeserializationFailedException("empty data")
        val lines = data.split("\\n").toTypedArray()
        val linesList: MutableList<IndentedLine> = ArrayList()
        //trim whitespaces and indents
        for (unindentedLine in lines) {
            val line = IndentedLine(unindentedLine)
            if (!line.indentedLine.isEmpty()) {
                linesList.add(line)
            }
        }
        deserializeItem(rootItem, linesList)
        return rootItem.getChild(0) // extract real root loaded from file
    }

    @Throws(DeserializationFailedException::class)
    private fun deserializeItem(parent: AbstractTreeItem, lines: List<IndentedLine>) {
        if (lines.isEmpty()) throw DeserializationFailedException("no lines for item")
        // first line - main attributes
        val headerLine = lines[0].indentedLine
        val attributes = extractAttributes(headerLine)
        if (attributes.isEmpty()) throw DeserializationFailedException("no attributes in header")
        val newItem = buildTreeItem(attributes)
        if (lines.size == 1) { // no children: { "type": "text", "name": "dupa" },
            if (!isHeaderMatchingSingleItem(headerLine)) throw DeserializationFailedException("invalid header format for single item: $headerLine")
        } else { // with children: { "type": "text", "name": "Dupa", "items": [
            if (!isHeaderMatchingMultiItem(headerLine)) throw DeserializationFailedException("invalid header format for item with children: $headerLine")
            if (lines.size <= 2) throw DeserializationFailedException("Insufficient children lines")

            // last line of part must be closing bracket
            if (lines[lines.size - 1].indentedLine != BLOCK_CLOSING_BRACKET) throw DeserializationFailedException(
                "No matching closing bracket found"
            )

            // split all child lines to parts
            val childLines = lines.subList(1, lines.size - 1) // assert size() >= 1
            val childParts = splitChildParts(
                childLines, lines[0]
                    .indentation + 1
            )
            if (childParts.isEmpty()) throw DeserializationFailedException("Child parts are empty")

            // deserialize all child parts and add them to this item
            for (childPart in childParts) {
                deserializeItem(newItem, childPart)
            }
        }
        newItem.setParent(parent)
        parent.add(newItem)
    }

    private fun unescape(s: String): String {
        var s = s
        s = s.replace("\\\"", "\"") // unescape \"
        s = s.replace("\\\\", "\\") // unescape \\
        return s
    }

    fun isHeaderMatchingSingleItem(line: String?): Boolean {
        return singleItemPattern.matcher(line).find()
    }

    fun isHeaderMatchingMultiItem(line: String?): Boolean {
        return multiItemPattern.matcher(line).find()
    }

    fun extractAttributes(line: String?): List<ItemAttribute> {
        val attrs: MutableList<ItemAttribute> = ArrayList()
        val m = nameValuePattern.matcher(line)
        while (m.find()) {
            val name = unescape(m.group(1))
            val value = unescape(m.group(2))
            attrs.add(ItemAttribute(name, value))
        }
        return attrs
    }

    @Throws(DeserializationFailedException::class)
    private fun buildTreeItem(attributes: List<ItemAttribute>): AbstractTreeItem {
        val typeAttr = attributes[0]
        if (typeAttr.name != "type") throw DeserializationFailedException("first attr not a type attribute")

        // build item based on type and attrs
        val type = typeAttr.value
        return when (type) {
            "/" -> {
                RootTreeItem()
            }
            "text" -> {
                val name = getAttributeValue(attributes, "name")
                TextTreeItem(null, name)
            }
            "remote" -> {
                val name = getAttributeValue(attributes, "name")
                RemoteTreeItem(null, name)
            }
            "separator" -> {
                SeparatorTreeItem(null)
            }
            "link" -> {
                val name = getOptionalAttributeValue(attributes, "name")
                val targetPath = getAttributeValue(attributes, "target")
                LinkTreeItem(null, targetPath, name)
            }
            "checkbox" -> {
                val name = getAttributeValue(attributes, "name")
                val checkedStr = getAttributeValue(attributes, "checked")
                CheckboxTreeItem(null, name, checkedStr == "true")
            }
            else -> throw DeserializationFailedException("Unknown item type: $type")
        }
    }

    @Throws(DeserializationFailedException::class)
    private fun getAttributeValue(
        attributes: List<ItemAttribute>,
        name: String
    ): String {
        return getOptionalAttributeValue(attributes, name)
            ?: throw DeserializationFailedException("Attribute not found: $name")
    }

    private fun getOptionalAttributeValue(attributes: List<ItemAttribute>, name: String): String? {
        for (attribute in attributes) {
            if (attribute.name == name) return attribute.value
        }
        return null
    }

    @Throws(DeserializationFailedException::class)
    private fun splitChildParts(
        lines: List<IndentedLine>,
        headerIndentation: Int
    ): List<List<IndentedLine>> {
        val childParts: MutableList<List<IndentedLine>> = ArrayList()
        // first line should be the header line
        if (lines[0].indentation != headerIndentation) throw DeserializationFailedException("invalid indentation of first child header")
        var i = 0
        while (i < lines.size) {
            val current = lines[i]
            if (current.indentation == headerIndentation) {
                if (current.indentedLine.endsWith("[")) { // item with children
                    val closingIndex = findClosingBracket(lines, i, headerIndentation)
                    childParts.add(lines.subList(i, closingIndex + 1))
                    i = closingIndex
                } else { //single item
                    childParts.add(lines.subList(i, i + 1))
                }
            }
            i++
        }
        return childParts
    }

    @Throws(DeserializationFailedException::class)
    private fun findClosingBracket(
        lines: List<IndentedLine>,
        startIndex: Int,
        headerIndentation: Int
    ): Int {
        for (j in startIndex + 1 until lines.size) {
            val line = lines[j]
            if (line.indentation == headerIndentation && (line.indentedLine
                        == BLOCK_CLOSING_BRACKET)
            ) {
                return j
            }
        }
        throw DeserializationFailedException("No matching closing bracket found")
    }

    init {
        singleItemPattern =
            Pattern.compile("^\\{ \"type\": \"([\\w/]+)\"(, \"(\\w+)\": \"((?:[^\"\\\\]|\\\\.)*)\")* \\},$")
        multiItemPattern =
            Pattern.compile("^\\{ \"type\": \"([\\w/]+)\"(, \"(\\w+)\": \"((?:[^\"\\\\]|\\\\.)*)\")*, \"items\": \\[$")
        nameValuePattern = Pattern.compile("\"(\\w+)\": \"((?:[^\"\\\\]|\\\\.)*)\"")
    }
}