package igrek.todotree.service.tree.persistence;


import org.junit.Before;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import igrek.todotree.domain.treeitem.AbstractTreeItem;
import igrek.todotree.domain.treeitem.RootTreeItem;
import igrek.todotree.domain.treeitem.TextTreeItem;
import igrek.todotree.exceptions.DeserializationFailedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JsonTreeSerializerTest {
	
	private JsonTreeSerializer serializer;
	private JsonTreeDeserializer deserializer;
	
	@Before
	public void init() {
		serializer = new JsonTreeSerializer();
		deserializer = new JsonTreeDeserializer();
	}
	
	@Test
	public void testDeserializationRegex() {
		
		assertTrue(deserializer.isHeaderMatchingSingleItem("{ \"type\": \"text\", \"name\": \"dupa\" },"));
		assertTrue(deserializer.isHeaderMatchingSingleItem("{ \"type\": \"text\", \"name\": \"name \\\"with\\\" escaped quote\" },"));
		assertFalse(deserializer.isHeaderMatchingSingleItem("{ \"type\": \"text\", \"name\": \"name \"with\" unescaped quote\" },"));
		assertTrue(deserializer.isHeaderMatchingSingleItem("{ \"type\": \"separator\" },"));
		assertTrue(deserializer.isHeaderMatchingSingleItem("{ \"type\": \"link\", \"name\": \"dupa\", \"target\": \"dupa2\" },"));
		assertFalse(deserializer.isHeaderMatchingSingleItem("{ \"type\": \"link\", \"name\": \"dupa\", \"target\": \"dupa2\" },dupa"));
		assertFalse(deserializer.isHeaderMatchingSingleItem("{ \"type\": \"text\", \"name\": \"Dupa\", \"items\": ["));
		
		assertTrue(deserializer.isHeaderMatchingMultiItem("{ \"type\": \"text\", \"name\": \"Dupa\", \"items\": ["));
		assertTrue(deserializer.isHeaderMatchingMultiItem("{ \"type\": \"d\", \"items\": ["));
		assertTrue(deserializer.isHeaderMatchingMultiItem("{ \"type\": \"/\", \"items\": ["));
		assertFalse(deserializer.isHeaderMatchingMultiItem("{ \"type\": \"text\", \"name\": \"dupa\" },"));
		
		assertEquals("[type = text, name = dupa]", deserializer.extractAttributes("{ \"type\": \"text\", \"name\": \"dupa\" },")
				.toString());
		assertEquals("[type = text with escaped \"quote\"]", deserializer.extractAttributes("{ \"type\": \"text with escaped \\\"quote\\\"\" },")
				.toString());
		assertEquals("[type = text, name = dupa with , name2 = dupa3]", deserializer.extractAttributes("{ \"type\": \"text\", \"name\": \"dupa with \"unescaped\"\", \"name2\": \"dupa3\" },")
				.toString());
		assertEquals("[type = back\\slash]", deserializer.extractAttributes("{ \"type\": \"back\\\\slash\" },")
				.toString());
		assertEquals("[]", deserializer.extractAttributes("{ type: \"invalid\" },").toString());
		
	}
	
	@Test
	public void testSimpleSerialization() {
		
		assertEquals("{ \"type\": \"/\" },\n", serializer.serializeTree(new RootTreeItem()));
		
		AbstractTreeItem root = new RootTreeItem();
		root.add(new TextTreeItem("dupa"));
		assertEquals("{ \"type\": \"/\", \"items\": [\n" + "\t{ \"type\": \"text\", \"name\": \"dupa\" },\n" + "]},\n", serializer
				.serializeTree(root));
		
		//System.out.println(serializer.serializeTree(root));
	}
	
	@Test
	public void testBidirectSerialization() throws DeserializationFailedException {
		
		assertEquals("{ \"type\": \"/\" },\n", serializer.serializeTree(new RootTreeItem()));
		
		AbstractTreeItem root = new RootTreeItem();
		root.add(new TextTreeItem("escaping \"quote\" back\\slash"));
		String serialized = serializer.serializeTree(root);
		assertEquals("{ \"type\": \"/\", \"items\": [\n" + "\t{ \"type\": \"text\", \"name\": \"escaping \\\"quote\\\" back\\\\slash\" },\n" + "]},\n", serialized);
		AbstractTreeItem deserialized = deserializer.deserializeTree(serialized);
		assertEquals(1, deserialized.size());
		AbstractTreeItem item = deserialized.getChild(0);
		assertTrue(item.isEmpty());
		assertTrue(item instanceof TextTreeItem);
		assertEquals("escaping \"quote\" back\\slash", item.displayName);
		
		//System.out.println(serializer.serializeTree(root));
	}
	
	private void testRegex(Pattern pattern, String line) {
		System.out.println("TESTING: " + line);
		
		Matcher m = pattern.matcher(line);
		while (m.find()) {
			System.out.println("Found");
			for (int i = 0; i <= m.groupCount(); i++) {
				System.out.println("Group " + i + ": " + m.group(i));
			}
		}
	}
	
}
