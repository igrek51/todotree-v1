package igrek.todotree.controller;


import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.model.treeitem.AbstractTreeItem;
import igrek.todotree.model.treeitem.TextTreeItem;
import igrek.todotree.services.clipboard.SystemClipboardManager;
import igrek.todotree.services.clipboard.TreeClipboardManager;
import igrek.todotree.services.resources.UserInfoService;
import igrek.todotree.services.tree.TreeManager;
import igrek.todotree.services.tree.TreeSelectionManager;
import igrek.todotree.ui.GUI;

public class ClipboardController {
	
	@Inject
	TreeManager treeManager;
	
	@Inject
	SystemClipboardManager systemClipboardManager;
	
	@Inject
	TreeClipboardManager treeClipboardManager;
	
	@Inject
	UserInfoService userInfo;
	
	@Inject
	GUI gui;
	
	@Inject
	TreeSelectionManager selectionManager;
	
	ClipboardController() {
		DaggerIOC.getAppComponent().inject(this);
	}
	
	public void copySelectedItems(boolean info) {
		if (selectionManager.isAnythingSelected()) {
			copyItems(selectionManager.getSelectedItems(), info);
		} else {
			userInfo.showInfo("No selected items");
		}
	}
	
	public void copyItems(Set<Integer> itemPosistions, boolean info) {
		if (!itemPosistions.isEmpty()) {
			treeClipboardManager.clearClipboard();
			for (Integer selectedItemId : itemPosistions) {
				AbstractTreeItem selectedItem = treeManager.getCurrentItem()
						.getChild(selectedItemId);
				treeClipboardManager.addToClipboard(selectedItem);
			}
			//if one item selected - copying also to system clipboard
			if (treeClipboardManager.getClipboardSize() == 1) {
				AbstractTreeItem item = treeClipboardManager.getClipboard().get(0);
				systemClipboardManager.copyToSystemClipboard(item.getDisplayName());
				if (info) {
					userInfo.showInfo("Item copied: " + item.getDisplayName());
				}
			} else {
				if (info) {
					userInfo.showInfo("Items copied: " + treeClipboardManager.getClipboardSize());
				}
			}
		} else {
			if (info) {
				userInfo.showInfo("No items to copy.");
			}
		}
	}
	
	public void cutSelectedItems() {
		if (selectionManager.isAnythingSelected()) {
			cutItems(selectionManager.getSelectedItems());
		} else {
			userInfo.showInfo("No selected items");
		}
	}
	
	public void cutItems(TreeSet<Integer> itemPosistions) {
		if (!itemPosistions.isEmpty()) {
			copyItems(itemPosistions, false);
			userInfo.showInfo("Items cut: " + itemPosistions.size());
			new ItemTrashController().removeItems(itemPosistions, false);
		}
	}
	
	public void pasteItems() {
		pasteItems(treeManager.positionAfterEnd());
	}
	
	public void pasteItems(int position) {
		if (treeClipboardManager.isClipboardEmpty()) {
			String systemClipboard = systemClipboardManager.getSystemClipboard();
			if (systemClipboard != null) {
				//wklejanie 1 elementu z systemowego schowka
				treeManager.addToCurrent(position, new TextTreeItem(systemClipboard));
				userInfo.showInfo("Item pasted: " + systemClipboard);
				new GUIController().updateItemsList();
				gui.scrollToItem(position);
			} else {
				userInfo.showInfo("Clipboard is empty.");
			}
		} else {
			for (AbstractTreeItem clipboardItem : treeClipboardManager.getClipboard()) {
				clipboardItem.setParent(treeManager.getCurrentItem());
				treeManager.addToCurrent(position, clipboardItem);
				position++; // next item pasted below
			}
			userInfo.showInfo("Items pasted: " + treeClipboardManager.getClipboardSize());
			treeClipboardManager.recopyClipboard();
			new GUIController().updateItemsList();
			gui.scrollToItem(position);
		}
	}
}
