package igrek.todotree.service.tree.persistence

import org.junit.Assert
import org.junit.Test

class TreeDeserializerTest {

    @Test
    fun testSingleItem() {
        assertValidDeserialize("""
type: "text"
name: "dupa"
""".trim())
    }

    @Test
    fun testRootWithItems() {
        assertValidDeserialize("""
type: "/"
items:
- name: "dupa"
""".trim())
    }

    @Test
    fun test3LevelItems() {
        val input = """
type: "text"
name: "New"
items:
- type: "text"
  name: "klucze"
  items:
  - name: "10, 13"
- type: "text"
  name: "dupa"
""".trim()
        assertValidDeserialize(input)
    }

    private fun assertValidDeserialize(input: String) {
        val deserializer = YamlTreeDeserializer()
        val item = deserializer.deserializeTree(input)
        println(item.toString())
        Assert.assertNotNull(item)
    }
}