package igrek.todotree.service.tree.persistence

import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.exceptions.DeserializationFailedException

class TreePersistenceService {

    private val deserializer = GsonTreeDeserializer()
    private val serializer = JsonTreeSerializer()

    fun serializeTree(root: AbstractTreeItem?): String {
        return serializer.serializeTree(root!!)
    }

    @Throws(DeserializationFailedException::class)
    fun deserializeTree(data: String?): AbstractTreeItem {
        return deserializer.deserializeTree(data!!)
    }
}