package igrek.todotree.service.tree;


import org.junit.Before;
import org.junit.Test;

import java.util.Set;
import java.util.TreeSet;

import igrek.todotree.activity.MainActivity;
import igrek.todotree.commands.ClipboardCommand;
import igrek.todotree.commands.TreeCommand;
import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.dagger.test.BaseDaggerTest;
import igrek.todotree.domain.treeitem.LinkTreeItem;
import igrek.todotree.domain.treeitem.RootTreeItem;
import igrek.todotree.domain.treeitem.TextTreeItem;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class TreeManagerTest extends BaseDaggerTest {
	
	@Before
	public void setUp() {
		MainActivity activity = mock(MainActivity.class);
		// Dagger init test
		DaggerIOC.initTest(null, activity);
		DaggerIOC.getTestComponent().inject(this);
	}
	
	@Test
	public void testLinkPasting() {
		// build db tree
		RootTreeItem itemR = new RootTreeItem();
		TextTreeItem itemRa = new TextTreeItem(null, "a");
		itemR.add(itemRa);
		TextTreeItem itemRa1 = new TextTreeItem(null, "a1");
		itemRa.add(itemRa1);
		TextTreeItem itemRb = new TextTreeItem(null, "b");
		itemR.add(itemRb);
		TextTreeItem itemRbc = new TextTreeItem(null, "bc");
		itemRb.add(itemRbc);
		treeManager.setRootItem(itemR);
		
		System.out.println("Serialized:\n" + persistenceService.serializeTree(itemR));
		
		// copy items
		treeManager.goTo(itemRa);
		Set<Integer> itemPosistions = new TreeSet<>();
		itemPosistions.add(0);
		new ClipboardCommand().copyItems(itemPosistions, true);
		
		// paste as link
		treeManager.goTo(itemRb);
		new ClipboardCommand().pasteItemsAsLink(0);
		LinkTreeItem link = (LinkTreeItem) treeManager.getCurrentItem().getChild(0);
		System.out.println("After pasting link:\n" + persistenceService.serializeTree(itemR));
		
		System.out.println("Link: " + link);
		System.out.println("Link target: " + link.getTarget());
		assertEquals(itemRa1, link.getTarget());
		System.out.println("Link target path: " + link.getTargetPath());
		System.out.println("Link display target path: " + link.getDisplayTargetPath());
		System.out.println("Link display name: " + link.getDisplayName());
		
		assertEquals(itemRb, treeManager.getCurrentItem());
		// click pasted link
		dbLock.unlockIfLocked(null);
		new TreeCommand().itemClicked(0, link);
		assertEquals(itemRa1, treeManager.getCurrentItem());
		System.out.println("current Item: " + treeManager.getCurrentItem());
		
	}
	
}
