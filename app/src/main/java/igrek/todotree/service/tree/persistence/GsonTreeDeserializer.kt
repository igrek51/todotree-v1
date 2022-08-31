package igrek.todotree.service.tree.persistence

import igrek.todotree.domain.treeitem.AbstractTreeItem.add
import com.google.gson.Gson
import kotlin.Throws
import igrek.todotree.exceptions.DeserializationFailedException
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.service.tree.persistence.GsonTreeDeserializer.JsonItem
import com.google.gson.JsonSyntaxException
import igrek.todotree.domain.treeitem.RootTreeItem
import igrek.todotree.domain.treeitem.TextTreeItem
import igrek.todotree.domain.treeitem.RemoteTreeItem
import igrek.todotree.domain.treeitem.SeparatorTreeItem
import igrek.todotree.domain.treeitem.LinkTreeItem
import igrek.todotree.domain.treeitem.CheckboxTreeItem
import com.google.gson.GsonBuilder

class GsonTreeDeserializer internal constructor() {
    private val gson: Gson
    @Throws(DeserializationFailedException::class)
    fun deserializeTree(data: String): AbstractTreeItem {
        // trim comma at the end
        var data = data
        data = data.trim { it <= ' ' }
        if (data.endsWith(",")) data = data.substring(0, data.length - 1)
        return try {
            val rootTreeItem = gson.fromJson(data, JsonItem::class.java)
                ?: throw DeserializationFailedException("root tree item is null")
            mapJsonItemToTreeItem(rootTreeItem)
        } catch (e: JsonSyntaxException) {
            throw DeserializationFailedException(e.message)
        }
    }

    @Throws(DeserializationFailedException::class)
    private fun mapJsonItemToTreeItem(jsonItem: JsonItem): AbstractTreeItem {
        val treeItem: AbstractTreeItem
        if (jsonItem.type == null) throw DeserializationFailedException("property 'type' not found")
        treeItem = when (jsonItem.type) {
            "/" -> {
                RootTreeItem()
            }
            "text" -> {
                if (jsonItem.name == null) throw DeserializationFailedException("property 'name' not found")
                TextTreeItem(null, jsonItem.name!!)
            }
            "remote" -> {
                if (jsonItem.name == null) throw DeserializationFailedException("property 'name' not found")
                RemoteTreeItem(null, jsonItem.name!!)
            }
            "separator" -> {
                SeparatorTreeItem(null)
            }
            "link" -> {

                // name is optional, target required
                if (jsonItem.target == null) throw DeserializationFailedException("property 'target' not found")
                LinkTreeItem(null, jsonItem.target!!, jsonItem.name)
            }
            "checkbox" -> {
                if (jsonItem.name == null) throw DeserializationFailedException("property 'name' not found")
                val checked = "true" == jsonItem.checked
                CheckboxTreeItem(null, jsonItem.name!!, checked)
            }
            else -> throw DeserializationFailedException("Unknown item type: " + jsonItem.type)
        }
        if (jsonItem.items != null) {
            for (jsonChild in jsonItem.items!!) {
                if (jsonChild != null) treeItem.add(mapJsonItemToTreeItem(jsonChild))
            }
        }
        return treeItem
    }

    private inner class JsonItem {
        var type: String? = null
        var name: String? = null
        var target: String? = null
        var checked: String? = null
        var items: List<JsonItem>? = null
    }

    init {
        val gsonb = GsonBuilder()
        gson = gsonb.create()
    }
}