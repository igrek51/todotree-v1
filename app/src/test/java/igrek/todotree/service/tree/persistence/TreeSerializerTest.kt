package igrek.todotree.service.tree.persistence

import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.domain.treeitem.RootTreeItem
import igrek.todotree.domain.treeitem.TextTreeItem
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class TreeSerializerTest {

    private var serializer: YamlTreeSerializer = YamlTreeSerializer()
    private var deserializer: YamlTreeDeserializer = YamlTreeDeserializer()

    @Before
    fun init() {
        serializer = YamlTreeSerializer()
        deserializer = YamlTreeDeserializer()
    }

    @Test
    fun testSimpleSerialization() {
        Assert.assertEquals("""---
type: "/"
""", serializer.serializeTree(RootTreeItem()))
        val root: AbstractTreeItem = RootTreeItem()
        root.add(TextTreeItem("dupa"))
        Assert.assertEquals("""---
type: "/"
items:
- name: "dupa"
""", serializer.serializeTree(root)
        )
        //System.out.println(serializer.serializeTree(root));
    }

    @Test
    fun testBidirectSerialization() {
        val root: AbstractTreeItem = RootTreeItem()
        val level1 = TextTreeItem("escaping \"quote\" back\\slash")
        root.add(level1)
        level1.add(TextTreeItem("abc"))
        level1.add(TextTreeItem("2"))
        val serialized = serializer.serializeTree(root)
        Assert.assertEquals("""---
type: "/"
items:
- name: "escaping \"quote\" back\\slash"
  items:
  - name: "abc"
  - name: "2"
""", serialized
        )
        println("serialized: $serialized")
        val deserialized = deserializer.deserializeTree(serialized)
        Assert.assertEquals(1, deserialized.size().toLong())
        val item = deserialized.getChild(0)
        Assert.assertTrue(!item.isEmpty)
        Assert.assertTrue(item is TextTreeItem)
        Assert.assertEquals("escaping \"quote\" back\\slash", item.displayName)
        //System.out.println(serializer.serializeTree(root));
    }
}