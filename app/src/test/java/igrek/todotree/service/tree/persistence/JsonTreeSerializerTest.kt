package igrek.todotree.service.tree.persistence

import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.domain.treeitem.RootTreeItem
import igrek.todotree.domain.treeitem.TextTreeItem
import igrek.todotree.exceptions.DeserializationFailedException
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class JsonTreeSerializerTest {

    private var serializer: JsonKtxTreeSerializer? = null
    private var deserializer: JsonKtxTreeDeserializer? = null

    @Before
    fun init() {
        serializer = JsonKtxTreeSerializer()
        deserializer = JsonKtxTreeDeserializer()
    }

    @Test
    fun testSimpleSerialization() {
        Assert.assertEquals("""{
    "type": "/"
}""".trimMargin(), serializer!!.serializeTree(RootTreeItem()))
        val root: AbstractTreeItem = RootTreeItem()
        root.add(TextTreeItem("dupa"))
        Assert.assertEquals("""{
    "type": "/",
    "items": [
        {
            "type": "text",
            "name": "dupa"
        }
    ]
}""", serializer!!.serializeTree(root)
        )
        //System.out.println(serializer.serializeTree(root));
    }

    @Test
    @Throws(DeserializationFailedException::class)
    fun testBidirectSerialization() {
        Assert.assertEquals("""{
    "type": "/"
}""", serializer!!.serializeTree(RootTreeItem()))
        val root: AbstractTreeItem = RootTreeItem()
        root.add(TextTreeItem("escaping \"quote\" back\\slash"))
        val serialized = serializer!!.serializeTree(root)
        Assert.assertEquals("""{
    "type": "/",
    "items": [
        {
            "type": "text",
            "name": "escaping \"quote\" back\\slash"
        }
    ]
}""", serialized
        )
        println("serialized: $serialized")
        val deserialized = deserializer!!.deserializeTree(serialized)
        Assert.assertEquals(1, deserialized.size().toLong())
        val item = deserialized.getChild(0)
        Assert.assertTrue(item.isEmpty)
        Assert.assertTrue(item is TextTreeItem)
        Assert.assertEquals("escaping \"quote\" back\\slash", item.displayName)
        //System.out.println(serializer.serializeTree(root));
    }
}