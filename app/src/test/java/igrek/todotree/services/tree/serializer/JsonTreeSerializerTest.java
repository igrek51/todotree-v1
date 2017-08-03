package igrek.todotree.services.tree.serializer;


import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		
		
		assertTrue(serializer.isMatchingSingleItem("{ \"type\": \"text\", \"name\": \"dupa\" },"));
		assertTrue(serializer.isMatchingSingleItem("{ \"type\": \"text\", \"name\": \"name \\\"with\\\" escaped quote\" },"));
		assertFalse(serializer.isMatchingSingleItem("{ \"type\": \"text\", \"name\": \"name \"with\" unescaped quote\" },"));
		assertTrue(serializer.isMatchingSingleItem("{ \"type\": \"separator\" },"));
		assertTrue(serializer.isMatchingSingleItem("{ \"type\": \"link\", \"name\": \"dupa\", \"target\": \"dupa2\" },"));
		assertFalse(serializer.isMatchingSingleItem("{ \"type\": \"link\", \"name\": \"dupa\", \"target\": \"dupa2\" },dupa"));
		
		
		List<ItemAttribute> attrs = serializer.extractAttributes("{ \"type\": \"text\", \"name\": \"dupa\" },");
		
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
