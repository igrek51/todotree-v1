package igrek.todotree.service.tree.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import igrek.todotree.domain.treeitem.*
import igrek.todotree.info.logger.Logger
import igrek.todotree.info.logger.LoggerFactory
import java.io.StringWriter

internal class YamlTreeSerializer {

    private val mapper = ObjectMapper(YAMLFactory())
    private val logger: Logger = LoggerFactory.logger

    fun serializeTree(root: AbstractTreeItem): String {
        val startTime = System.currentTimeMillis()

        val writer = StringWriter()

        val rawRoot: Map<String, Any> = convertToSerializableItem(root)
        mapper.writeValue(writer, rawRoot)
        val yamlString = writer.toString()

        val duration = System.currentTimeMillis() - startTime
        logger.debug("Tree serialization done in $duration ms")
        return yamlString
    }

    private fun convertToSerializableItem(item: AbstractTreeItem): Map<String, Any> {
        val contentMap: MutableMap<String, Any> = mutableMapOf()

        if (item.typeName != "text") {
            contentMap["type"] = item.typeName
        }

        serializeAttributes(contentMap, item)

        // child items
        if (!item.isEmpty) {
            val children: List<Map<String, Any>> = item.children.map {
                convertToSerializableItem(it)
            }
            contentMap["items"] = children
        }

        return contentMap
    }

    private fun serializeAttributes(contentMap: MutableMap<String, Any>, item: AbstractTreeItem) {
        if (item is RemoteTreeItem) {
            contentMap["name"] = item.displayName
        } else if (item is TextTreeItem) {
            contentMap["name"] = item.displayName
        } else if (item is LinkTreeItem) {
            contentMap["target"] = item.targetPath
            if (item.hasCustomName()) {
                contentMap["name"] = item.customName.orEmpty()
            }
        } else if (item is CheckboxTreeItem) {
            contentMap["checked"] = if (item.isChecked) "true" else "false"
        }
    }
}