package igrek.todotree.service.tree.persistence;


import org.junit.Before;
import org.junit.Test;

import igrek.todotree.domain.treeitem.AbstractTreeItem;

import static org.junit.Assert.assertNotNull;

public class GsonDeserializerTest {
	
	private GsonTreeDeserializer deserializer;
	
	@Before
	public void init() {
		deserializer = new GsonTreeDeserializer();
	}
	
	@Test
	public void testSingleItem() {
		assertValidDeserialize("{ \"type\": \"text\", \"name\": \"dupa\" },");
	}
	
	@Test
	public void testRootWithItems() {
		assertValidDeserialize("{ \"type\": \"/\", \"items\": [\n" + "\t{ \"type\": \"text\", \"name\": \"dupa\" },\n" + "]},\n");
	}
	
	@Test
	public void test3LevelItems() {
		String input = "{ \"type\": \"text\", \"name\": \"New\", \"items\": [\n" + "\t{ \"type\": \"text\", \"name\": \"klucze\", \"items\": [\n" + "\t\t{ \"type\": \"text\", \"name\": \"10, 13\" },\n" + "\t]},\n" + "\t{ \"type\": \"text\", \"name\": \"dupa\" },\n" + "]},";
		assertValidDeserialize(input);
	}
	
	@Test
	public void testOptionalLinkName() {
		// TODO throw away TreeCommand from LinkTreeItem
		//assertValidDeserialize("{ \"type\": \"link\", \"target\": \"Quests\tDupa\" },");
	}
	
	private void assertValidDeserialize(String input) {
		AbstractTreeItem item = deserializer.deserializeTree(input);
		
		System.out.println(item.toString());
		
		assertNotNull(item);
	}
	
	
}
