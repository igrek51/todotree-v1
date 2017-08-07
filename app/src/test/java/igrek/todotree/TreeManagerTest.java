package igrek.todotree;


import org.junit.Before;
import org.junit.Test;

import java.util.Set;
import java.util.TreeSet;

import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.logger.Logs;
import igrek.todotree.model.treeitem.AbstractTreeItem;
import igrek.todotree.model.treeitem.LinkTreeItem;
import igrek.todotree.model.treeitem.RootTreeItem;
import igrek.todotree.model.treeitem.TextTreeItem;
import igrek.todotree.services.clipboard.TreeClipboardManager;
import igrek.todotree.services.history.ChangesHistory;
import igrek.todotree.services.tree.TreeManager;
import igrek.todotree.services.tree.TreeScrollCache;
import igrek.todotree.services.tree.serializer.JsonTreeSerializer;

public class TreeManagerTest {
	
	TreeManager treeManager;
	TreeClipboardManager treeClipboardManager;
	TreeScrollCache scrollCache;
	JsonTreeSerializer serializer;
	
	@Before
	public void setUp() throws Exception {
		// Dagger init test
		DaggerIOC.initTest();
		
		treeManager = new TreeManager(new ChangesHistory());
		treeClipboardManager = new TreeClipboardManager();
		scrollCache = new TreeScrollCache();
		serializer = new JsonTreeSerializer();
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
		copyItems(itemPosistions, true);
		
		// paste as link
		treeManager.goTo(itemRb);
		pasteItemsAsLink(0);
		LinkTreeItem link = (LinkTreeItem) treeManager.getCurrentItem().getChild(0);
		System.out.println("After pasting link:\n" + serializer.serializeTree(itemR));
		
		System.out.println("Link target: " + getTarget(link.getTargetPath()));
		System.out.println("Link display name" + link.getDisplayName());
		
	}
	
	private void copyItems(Set<Integer> itemPosistions, boolean info) {
		if (!itemPosistions.isEmpty()) {
			treeClipboardManager.clearClipboard();
			AbstractTreeItem currentItem = treeManager.getCurrentItem();
			treeClipboardManager.setCopiedFrom(currentItem);
			for (Integer selectedItemId : itemPosistions) {
				AbstractTreeItem selectedItem = currentItem.getChild(selectedItemId);
				treeClipboardManager.addToClipboard(selectedItem);
			}
			if (info)
				Logs.test("Items copied: " + treeClipboardManager.getClipboardSize());
		} else {
			if (info)
				Logs.test("No items to copy.");
		}
	}
	
	private void pasteItemsAsLink(int position) {
		if (treeClipboardManager.isClipboardEmpty()) {
			Logs.test("Clipboard is empty.");
		} else {
			for (AbstractTreeItem clipboardItem : treeClipboardManager.getClipboard()) {
				treeManager.addToCurrent(position, buildLinkItem(clipboardItem));
				position++; // next item pasted below
			}
			Logs.test("Items pasted as links: " + treeClipboardManager.getClipboardSize());
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
