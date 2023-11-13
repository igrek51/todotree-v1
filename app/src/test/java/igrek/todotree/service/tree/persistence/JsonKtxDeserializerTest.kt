package igrek.todotree.service.tree.persistence

import org.junit.Assert
import org.junit.Test

class JsonKtxDeserializerTest {

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
        val deserializer = JsonKtxTreeDeserializer()
        val item = deserializer.deserializeTree(input)
        println(item.toString())
        Assert.assertNotNull(item)
    }
}