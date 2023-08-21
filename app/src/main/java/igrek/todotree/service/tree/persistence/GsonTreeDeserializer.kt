package igrek.todotree.service.tree.persistence

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import igrek.todotree.domain.treeitem.*
import igrek.todotree.exceptions.DeserializationFailedException

class GsonTreeDeserializer internal constructor() {

    private val gson: Gson

    @Throws(DeserializationFailedException::class)
    fun deserializeTree(data: String): AbstractTreeItem {
        // trim comma at the end
        var mData = data
        mData = mData.trim { it <= ' ' }
        if (mData.endsWith(",")) mData = mData.substring(0, mData.length - 1)
        return try {
            val rootTreeItem = gson.fromJson(mData, JsonItem::class.java)
                ?: throw DeserializationFailedException("root tree item is null")
            mapJsonItemToTreeItem(rootTreeItem)
        } catch (e: JsonSyntaxException) {
            throw DeserializationFailedException(e.message)
        }
    }

    @Throws(DeserializationFailedException::class)
    private fun mapJsonItemToTreeItem(jsonItem: JsonItem): AbstractTreeItem {
        if (jsonItem.type == null) throw DeserializationFailedException("property 'type' not found")
        val treeItem: AbstractTreeItem = when (jsonItem.type) {
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
                if (jsonChild != null) {
                    treeItem.add(mapJsonItemToTreeItem(jsonChild))
                }
            }
        }
        return treeItem
    }

    private inner class JsonItem {
        var type: String? = null
        var name: String? = null
        var target: String? = null
        var checked: String? = null
        var items: List<JsonItem?>? = null
    }

    init {
        val gsonb = GsonBuilder()
        gson = gsonb.create()
    }
}