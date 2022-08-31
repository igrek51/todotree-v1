package igrek.todotree.service.tree.persistence

import org.junit.Assert
import org.junit.Before
import org.junit.Test

class GsonDeserializerTest {

    private var deserializer: GsonTreeDeserializer? = null

    @Before
    fun init() {
        deserializer = GsonTreeDeserializer()
    }

    @Test
    fun testSingleItem() {
        assertValidDeserialize("{ \"type\": \"text\", \"name\": \"dupa\" },")
    }

    @Test
    fun testRootWithItems() {
        assertValidDeserialize(
            """{ "type": "/", "items": [
	{ "type": "text", "name": "dupa" },
]},
"""
        )
    }

    @Test
    fun test3LevelItems() {
        val input = """{ "type": "text", "name": "New", "items": [
	{ "type": "text", "name": "klucze", "items": [
		{ "type": "text", "name": "10, 13" },
	]},
	{ "type": "text", "name": "dupa" },
]},"""
        assertValidDeserialize(input)
    }

    private fun assertValidDeserialize(input: String) {
        val item = deserializer!!.deserializeTree(input)
        println(item.toString())
        Assert.assertNotNull(item)
    }
}