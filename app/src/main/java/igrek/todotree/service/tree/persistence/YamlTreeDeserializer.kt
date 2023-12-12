package igrek.todotree.service.tree.persistence

import com.charleskorn.kaml.Yaml
import igrek.todotree.domain.treeitem.*
import igrek.todotree.exceptions.DeserializationFailedException
import igrek.todotree.info.logger.Logger
import igrek.todotree.info.logger.LoggerFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException


class YamlTreeDeserializer {

    private val yaml = Yaml.default
    private val logger: Logger = LoggerFactory.logger

    @Throws(DeserializationFailedException::class)
    fun deserializeTree(data: String): AbstractTreeItem {
        val startTime = System.currentTimeMillis()

        try {
            val rawRoot: SerializableItem = yaml.decodeFromString(SerializableItem.serializer(), data)

            val result = mapRawItemToTreeItem(rawRoot)

            val duration = System.currentTimeMillis() - startTime
            logger.debug("Tree deserialization done in $duration ms")
            return result
        } catch (e: SerializationException) {
            throw DeserializationFailedException(e.message)
        }
    }

    @Throws(DeserializationFailedException::class)
    private fun mapRawItemToTreeItem(rawItem: SerializableItem): AbstractTreeItem {
        val type = rawItem.type ?: "text"

        val treeItem: AbstractTreeItem = when (type) {
            "/" -> {
                RootTreeItem()
            }
            "text" -> {
                if (rawItem.name == null) throw DeserializationFailedException("property 'name' not found")
                TextTreeItem(null, rawItem.name)
            }
            "remote" -> {
                if (rawItem.name == null) throw DeserializationFailedException("property 'name' not found")
                RemoteTreeItem(null, rawItem.name)
            }
            "separator" -> {
                SeparatorTreeItem(null)
            }
            "link" -> {
                // name is optional, target required
                if (rawItem.target == null) throw DeserializationFailedException("property 'target' not found")
                LinkTreeItem(null, rawItem.target, rawItem.name)
            }
            "checkbox" -> {
                if (rawItem.name == null) throw DeserializationFailedException("property 'name' not found")
                val checked = "true" == rawItem.checked
                CheckboxTreeItem(null, rawItem.name, checked)
            }
            else -> throw DeserializationFailedException("Unknown item type: " + rawItem.type)
        }
        if (rawItem.items != null) {
            for (jsonChild in rawItem.items) {
                if (jsonChild != null) {
                    treeItem.add(mapRawItemToTreeItem(jsonChild))
                }
            }
        }
        return treeItem
    }

    @Serializable
    private data class SerializableItem(
        val type: String? = null,
        val name: String? = null,
        val target: String? = null,
        val checked: String? = null,
        val items: List<SerializableItem?>? = null,
    )
}
