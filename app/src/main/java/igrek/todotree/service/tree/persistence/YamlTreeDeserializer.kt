package igrek.todotree.service.tree.persistence

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import igrek.todotree.domain.treeitem.*
import igrek.todotree.exceptions.DeserializationFailedException
import igrek.todotree.info.logger.Logger
import igrek.todotree.info.logger.LoggerFactory


class YamlTreeDeserializer {

    private val mapper = ObjectMapper(YAMLFactory())
    private val logger: Logger = LoggerFactory.logger

    @Throws(DeserializationFailedException::class)
    fun deserializeTree(data: String): AbstractTreeItem {
        val startTime = System.currentTimeMillis()

        try {
            val rootNode: JsonNode = mapper.readTree(data)
            val result = mapNodeToTreeItem(rootNode)

            val duration = System.currentTimeMillis() - startTime
            logger.debug("Tree deserialization done in $duration ms")
            return result
        } catch (e: Exception) {
            throw DeserializationFailedException(e.message)
        }
    }

    @Throws(DeserializationFailedException::class)
    private fun mapNodeToTreeItem(node: JsonNode): AbstractTreeItem {
        val type = node.get("type")?.asText() ?: "text"

        val treeItem: AbstractTreeItem = when (type) {
            "/" -> {
                RootTreeItem()
            }
            "text" -> {
                val name = node.get("name")?.asText()
                    ?: throw DeserializationFailedException("property 'name' not found")
                TextTreeItem(null, name)
            }
            "remote" -> {
                val name = node.get("name")?.asText()
                    ?: throw DeserializationFailedException("property 'name' not found")
                RemoteTreeItem(null, name)
            }
            "separator" -> {
                SeparatorTreeItem(null)
            }
            "link" -> {
                // name is optional, target required
                val name = node.get("name")?.asText()
                val target = node.get("target")?.asText()
                    ?: throw DeserializationFailedException("property 'target' not found")
                LinkTreeItem(null, target, name)
            }
            "checkbox" -> {
                val name = node.get("name")?.asText()
                    ?: throw DeserializationFailedException("property 'name' not found")
                val checkedStr = node.get("checked")?.asText()
                val checkedBool = "true" == checkedStr
                CheckboxTreeItem(null, name, checkedBool)
            }
            else -> throw DeserializationFailedException("Unknown item type: $type")
        }
        if (node.get("items") != null) {
            val items: ArrayNode = node.get("items") as ArrayNode
            for (child in items.elements()) {
                if (child != null) {
                    treeItem.add(mapNodeToTreeItem(child))
                }
            }
        }
        return treeItem
    }
}
