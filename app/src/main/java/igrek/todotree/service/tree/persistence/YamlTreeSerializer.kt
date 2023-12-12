package igrek.todotree.service.tree.persistence

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import igrek.todotree.domain.treeitem.*
import igrek.todotree.info.logger.Logger
import igrek.todotree.info.logger.LoggerFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

internal class YamlTreeSerializer {

    private val yaml = Yaml(
        configuration = YamlConfiguration(
            encodeDefaults=false,
        ),
    )
    private val logger: Logger = LoggerFactory.logger

    fun serializeTree(root: AbstractTreeItem): String {
        val startTime = System.currentTimeMillis()

        val rawRoot: SerializableItem = convertToSerializableItem(root)
        val yamlString = yaml.encodeToString(SerializableItem.serializer(), rawRoot)

        val duration = System.currentTimeMillis() - startTime
        logger.debug("Tree serialization done in $duration ms")
        return yamlString
    }

    private fun convertToSerializableItem(item: AbstractTreeItem): SerializableItem {
        val content = SerializableItem()

        if (item.typeName != "text") {
            content.type = item.typeName
        }

        serializeAttributes(content, item)

        // child items
        if (!item.isEmpty) {
            val children: List<SerializableItem> = item.children.map {
                convertToSerializableItem(it)
            }
            content.items = children
        }

        return content
    }

    private fun serializeAttributes(content: SerializableItem, item: AbstractTreeItem) {
        if (item is RemoteTreeItem) {
            content.name = item.displayName
        } else if (item is TextTreeItem) {
            content.name = item.displayName
        } else if (item is LinkTreeItem) {
            content.target = item.targetPath
            if (item.hasCustomName()) {
                content.name = item.customName.orEmpty()
            }
        } else if (item is CheckboxTreeItem) {
            content.checked = if (item.isChecked) "true" else "false"
        }
    }

    @Serializable
    private data class SerializableItem(
        var type: String? = null,
        var name: String? = null,
        var target: String? = null,
        var checked: String? = null,
        var items: List<SerializableItem?>? = null,
    )
}