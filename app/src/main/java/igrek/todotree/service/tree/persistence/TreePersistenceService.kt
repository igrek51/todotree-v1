package igrek.todotree.service.tree.persistence

import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.exceptions.DeserializationFailedException

class TreePersistenceService {

    private val serializer = YamlTreeSerializer()
    private val deserializer = YamlTreeDeserializer()
    private val fallbackDeserializer = JsonKtxTreeDeserializer()

    fun serializeTree(root: AbstractTreeItem): String {
        return serializer.serializeTree(root)
    }

    @Throws(DeserializationFailedException::class)
    fun deserializeTree(data: String): AbstractTreeItem {
        return try {
            deserializer.deserializeTree(data)
        } catch (e: DeserializationFailedException) {
            fallbackDeserializer.deserializeTree(data)
        }
    }
}