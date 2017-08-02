package igrek.todotree.controller;


import javax.inject.Inject;

import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.datatree.TreeClipboardManager;
import igrek.todotree.datatree.TreeManager;
import igrek.todotree.datatree.TreeSelectionManager;
import igrek.todotree.datatree.item.TreeItem;
import igrek.todotree.gui.GUI;
import igrek.todotree.services.clipboard.SystemClipboardManager;
import igrek.todotree.services.resources.UserInfoService;

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
			treeClipboardManager.clearClipboard();
			for (Integer selectedItemId : selectionManager.getSelectedItems()) {
				TreeItem selectedItem = treeManager.getCurrentItem().getChild(selectedItemId);
				treeClipboardManager.addToClipboard(selectedItem);
			}
			//jeśli zaznaczony jeden element - skopiowanie do schowka
			if (treeClipboardManager.getClipboardSize() == 1) {
				TreeItem item = treeClipboardManager.getClipboard().get(0);
				systemClipboardManager.copyToSystemClipboard(item.getContent());
				if (info) {
					userInfo.showInfo("Item copied: " + item.getContent());
				}
			} else {
				if (info) {
					userInfo.showInfo("Selected items copied: " + treeClipboardManager.getClipboardSize());
				}
			}
		} else {
			userInfo.showInfo("No selected items");
		}
	}
	
	
	public void cutSelectedItems() {
		if (selectionManager.isAnythingSelected()) {
			copySelectedItems(false);
			userInfo.showInfo("Selected items cut: " + selectionManager.getSelectedItemsCount());
			new ItemTrashController().removeSelectedItems(false);
		} else {
			userInfo.showInfo("No selected items");
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
				treeManager.addToCurrent(position, systemClipboard);
				userInfo.showInfo("Item pasted: " + systemClipboard);
				new GUIController().updateItemsList();
				gui.scrollToItem(-1);
			} else {
				userInfo.showInfo("Clipboard is empty.");
			}
		} else {
			for (TreeItem clipboardItem : treeClipboardManager.getClipboard()) {
				clipboardItem.setParent(treeManager.getCurrentItem());
				treeManager.addToCurrent(position, clipboardItem);
				position++; // next item pasted below
			}
			userInfo.showInfo("Items pasted: " + treeClipboardManager.getClipboardSize());
			treeClipboardManager.recopyClipboard();
			new GUIController().updateItemsList();
			gui.scrollToItem(-1);
		}
	}
}
