package igrek.todotree.service.tree.persistence

import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.domain.treeitem.RootTreeItem
import igrek.todotree.domain.treeitem.TextTreeItem
import igrek.todotree.exceptions.DeserializationFailedException
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.regex.Pattern

class JsonTreeSerializerTest {

    private var serializer: JsonTreeSerializer? = null
    private var deserializer: JsonTreeDeserializer? = null

    @Before
    fun init() {
        serializer = JsonTreeSerializer()
        deserializer = JsonTreeDeserializer()
    }

    @Test
    fun testDeserializationRegex() {
        Assert.assertTrue(deserializer!!.isHeaderMatchingSingleItem("{ \"type\": \"text\", \"name\": \"dupa\" },"))
        Assert.assertTrue(deserializer!!.isHeaderMatchingSingleItem("{ \"type\": \"text\", \"name\": \"name \\\"with\\\" escaped quote\" },"))
        Assert.assertFalse(deserializer!!.isHeaderMatchingSingleItem("{ \"type\": \"text\", \"name\": \"name \"with\" unescaped quote\" },"))
        Assert.assertTrue(deserializer!!.isHeaderMatchingSingleItem("{ \"type\": \"separator\" },"))
        Assert.assertTrue(deserializer!!.isHeaderMatchingSingleItem("{ \"type\": \"link\", \"name\": \"dupa\", \"target\": \"dupa2\" },"))
        Assert.assertFalse(deserializer!!.isHeaderMatchingSingleItem("{ \"type\": \"link\", \"name\": \"dupa\", \"target\": \"dupa2\" },dupa"))
        Assert.assertFalse(deserializer!!.isHeaderMatchingSingleItem("{ \"type\": \"text\", \"name\": \"Dupa\", \"items\": ["))
        Assert.assertTrue(deserializer!!.isHeaderMatchingMultiItem("{ \"type\": \"text\", \"name\": \"Dupa\", \"items\": ["))
        Assert.assertTrue(deserializer!!.isHeaderMatchingMultiItem("{ \"type\": \"d\", \"items\": ["))
        Assert.assertTrue(deserializer!!.isHeaderMatchingMultiItem("{ \"type\": \"/\", \"items\": ["))
        Assert.assertFalse(deserializer!!.isHeaderMatchingMultiItem("{ \"type\": \"text\", \"name\": \"dupa\" },"))
        Assert.assertEquals(
            "[type = text, name = dupa]",
            deserializer!!.extractAttributes("{ \"type\": \"text\", \"name\": \"dupa\" },")
                .toString()
        )
        Assert.assertEquals(
            "[type = text with escaped \"quote\"]",
            deserializer!!.extractAttributes("{ \"type\": \"text with escaped \\\"quote\\\"\" },")
                .toString()
        )
        Assert.assertEquals(
            "[type = text, name = dupa with , name2 = dupa3]",
            deserializer!!.extractAttributes("{ \"type\": \"text\", \"name\": \"dupa with \"unescaped\"\", \"name2\": \"dupa3\" },")
                .toString()
        )
        Assert.assertEquals(
            "[type = back\\slash]",
            deserializer!!.extractAttributes("{ \"type\": \"back\\\\slash\" },")
                .toString()
        )
        Assert.assertEquals(
            "[]",
            deserializer!!.extractAttributes("{ type: \"invalid\" },").toString()
        )
    }

    @Test
    fun testSimpleSerialization() {
        Assert.assertEquals("{ \"type\": \"/\" },\n", serializer!!.serializeTree(RootTreeItem()))
        val root: AbstractTreeItem = RootTreeItem()
        root.add(TextTreeItem("dupa"))
        Assert.assertEquals(
            """{ "type": "/", "items": [
	{ "type": "text", "name": "dupa" },
]},
""", serializer!!.serializeTree(root)
        )

        //System.out.println(serializer.serializeTree(root));
    }

    @Test
    @Throws(DeserializationFailedException::class)
    fun testBidirectSerialization() {
        Assert.assertEquals("{ \"type\": \"/\" },\n", serializer!!.serializeTree(RootTreeItem()))
        val root: AbstractTreeItem = RootTreeItem()
        root.add(TextTreeItem("escaping \"quote\" back\\slash"))
        val serialized = serializer!!.serializeTree(root)
        Assert.assertEquals(
            """{ "type": "/", "items": [
	{ "type": "text", "name": "escaping \"quote\" back\\slash" },
]},
""", serialized
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

    private fun testRegex(pattern: Pattern, line: String) {
        println("TESTING: $line")
        val m = pattern.matcher(line)
        while (m.find()) {
            println("Found")
            for (i in 0..m.groupCount()) {
                println("Group " + i + ": " + m.group(i))
            }
        }
    }
}