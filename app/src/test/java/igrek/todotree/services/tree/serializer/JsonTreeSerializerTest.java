package igrek.todotree.services.tree.serializer;


import org.junit.Before;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JsonTreeSerializerTest {
	
	private JsonTreeSerializer serializer;
	
	@Before
	public void init() throws Exception {
		serializer = new JsonTreeSerializer();
	}
	
	@Test
	public void testDeserializationRegex() {
		
		assertTrue(serializer.isHeaderMatchingSingleItem("{ \"type\": \"text\", \"name\": \"dupa\" },"));
		assertTrue(serializer.isHeaderMatchingSingleItem("{ \"type\": \"text\", \"name\": \"name \\\"with\\\" escaped quote\" },"));
		assertFalse(serializer.isHeaderMatchingSingleItem("{ \"type\": \"text\", \"name\": \"name \"with\" unescaped quote\" },"));
		assertTrue(serializer.isHeaderMatchingSingleItem("{ \"type\": \"separator\" },"));
		assertTrue(serializer.isHeaderMatchingSingleItem("{ \"type\": \"link\", \"name\": \"dupa\", \"target\": \"dupa2\" },"));
		assertFalse(serializer.isHeaderMatchingSingleItem("{ \"type\": \"link\", \"name\": \"dupa\", \"target\": \"dupa2\" },dupa"));
		assertFalse(serializer.isHeaderMatchingSingleItem("{ \"type\": \"text\", \"name\": \"Dupa\", \"items\": ["));
		
		assertTrue(serializer.isHeaderMatchingMultiItem("{ \"type\": \"text\", \"name\": \"Dupa\", \"items\": ["));
		assertFalse(serializer.isHeaderMatchingMultiItem("{ \"type\": \"text\", \"name\": \"dupa\" },"));
		
		assertEquals("[type = text, name = dupa]", serializer.extractAttributes("{ \"type\": \"text\", \"name\": \"dupa\" },")
				.toString());
		assertEquals("[type = text with escaped \"quote\"]", serializer.extractAttributes("{ \"type\": \"text with escaped \\\"quote\\\"\" },")
				.toString());
		assertEquals("[type = text, name = dupa with , name2 = dupa3]", serializer.extractAttributes("{ \"type\": \"text\", \"name\": \"dupa with \"unescaped\"\", \"name2\": \"dupa3\" },")
				.toString());
		assertEquals("[type = back\\slash]", serializer.extractAttributes("{ \"type\": \"back\\\\slash\" },")
				.toString());
		assertEquals("[]", serializer.extractAttributes("{ type: \"invalid\" },").toString());
		
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
