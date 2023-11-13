package igrek.todotree.service.tree.persistence

import igrek.todotree.domain.treeitem.*
import igrek.todotree.info.logger.Logger
import igrek.todotree.info.logger.LoggerFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

internal class JsonKtxTreeSerializer {

    private val json = Json {
        ignoreUnknownKeys = true
        allowStructuredMapKeys = true
        prettyPrint = true
        useArrayPolymorphism = false
        isLenient = true
    }
    private val logger: Logger = LoggerFactory.logger

    fun serializeTree(root: AbstractTreeItem): String {
        val startTime = System.currentTimeMillis()

        val jsonItem: JsonObject = convertToJsonItem(root)
        val result = json.encodeToString(JsonObject.serializer(), jsonItem)

        val duration = System.currentTimeMillis() - startTime
        logger.debug("Tree serialization done in $duration ms")
        return result
    }

    private fun convertToJsonItem(item: AbstractTreeItem): JsonObject {
        val contentMap: MutableMap<String, JsonElement> = mutableMapOf(
            "type" to JsonPrimitive(item.typeName),
        )

        serializeAttributes(contentMap, item)

        // child items
        if (!item.isEmpty) {
            val children: List<JsonObject> = item.children.map {
                convertToJsonItem(it)
            }
            contentMap["items"] = JsonArray(children)
        }

        return JsonObject(contentMap)
    }

    private fun serializeAttributes(contentMap: MutableMap<String, JsonElement>, item: AbstractTreeItem) {
        if (item is RemoteTreeItem) {
            contentMap["name"] = JsonPrimitive(item.displayName)
        } else if (item is TextTreeItem) {
            contentMap["name"] = JsonPrimitive(item.displayName)
        } else if (item is LinkTreeItem) {
            contentMap["target"] = JsonPrimitive(item.targetPath)
            if (item.hasCustomName()) {
                contentMap["name"] = JsonPrimitive(item.customName)
            }
        } else if (item is CheckboxTreeItem) {
            contentMap["checked"] = JsonPrimitive(if (item.isChecked) "true" else "false")
        }
    }

    @Serializable
    private data class JsonItem(
        var type: String,
        var name: String? = null,
        var target: String? = null,
        var checked: String? = null,
        var items: List<JsonItem?>? = null,
    )
}