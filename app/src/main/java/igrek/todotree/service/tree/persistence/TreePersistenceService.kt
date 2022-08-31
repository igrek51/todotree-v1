package igrek.todotree.service.tree.persistence

import igrek.todotree.service.tree.persistence.JsonTreeSerializer.serializeTree
import igrek.todotree.service.tree.persistence.GsonTreeDeserializer.deserializeTree
import igrek.todotree.service.tree.persistence.GsonTreeDeserializer
import igrek.todotree.service.tree.persistence.JsonTreeSerializer
import igrek.todotree.domain.treeitem.AbstractTreeItem
import kotlin.Throws
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