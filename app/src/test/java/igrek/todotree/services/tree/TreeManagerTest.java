package igrek.todotree.services.tree;


import org.junit.Before;
import org.junit.Test;

import java.util.Set;
import java.util.TreeSet;

import igrek.todotree.MainActivity;
import igrek.todotree.controller.ClipboardController;
import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.dagger.test.BaseDaggerTest;
import igrek.todotree.logger.Logs;
import igrek.todotree.model.treeitem.AbstractTreeItem;
import igrek.todotree.model.treeitem.LinkTreeItem;
import igrek.todotree.model.treeitem.RootTreeItem;
import igrek.todotree.model.treeitem.TextTreeItem;

import static org.mockito.Mockito.mock;

public class TreeManagerTest extends BaseDaggerTest {
	
	@Before
	public void setUp() throws Exception {
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
		
		System.out.println("Serialized:\n" + serializer.serializeTree(itemR));
		
		// copy items
		treeManager.goTo(itemRa);
		Set<Integer> itemPosistions = new TreeSet<>();
		itemPosistions.add(0);
		new ClipboardController().copyItems(itemPosistions, true);
		
		// paste as link
		treeManager.goTo(itemRb);
		pasteItemsAsLink(0);
		LinkTreeItem link = (LinkTreeItem) treeManager.getCurrentItem().getChild(0);
		System.out.println("After pasting link:\n" + serializer.serializeTree(itemR));
		
		System.out.println("Link target: " + getTarget(link.getTargetPath()));
		System.out.println("Link display name" + link.getDisplayName());
		
	}
	
	private void pasteItemsAsLink(int position) {
		if (treeClipboardManager.isClipboardEmpty()) {
			logger.info("Clipboard is empty.");
		} else {
			for (AbstractTreeItem clipboardItem : treeClipboardManager.getClipboard()) {
				treeManager.addToCurrent(position, buildLinkItem(clipboardItem));
				position++; // next item pasted below
			}
			logger.info("Items pasted as links: " + treeClipboardManager.getClipboardSize());
		}
	}
	
	private AbstractTreeItem buildLinkItem(AbstractTreeItem clipboardItem) {
		LinkTreeItem link = new LinkTreeItem(treeManager.getCurrentItem(), null, null);
		link.setTarget(treeClipboardManager.getCopiedFrom(), clipboardItem.getDisplayName());
		return link;
	}
	
	private AbstractTreeItem getTarget(String targetPath) {
		String[] paths = targetPath.split("\\t");
		return findItemByPath(paths);
	}
	
	private AbstractTreeItem findItemByPath(String[] paths) {
		AbstractTreeItem current = treeManager.getRootItem();
		for (String path : paths) {
			AbstractTreeItem found = current.findChildByName(path);
			if (found == null)
				return null;
			current = found;
		}
		return current;
	}
	
}
