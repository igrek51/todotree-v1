package igrek.todotree.controller;


import javax.inject.Inject;

import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.datatree.TreeManager;
import igrek.todotree.datatree.item.TreeItem;
import igrek.todotree.gui.GUI;
import igrek.todotree.services.clipboard.ClipboardManager;
import igrek.todotree.services.resources.UserInfoService;

public class ClipboardController {
	
	@Inject
	TreeManager treeManager;
	
	@Inject
	ClipboardManager clipboardManager;
	
	@Inject
	UserInfoService userInfo;
	
	@Inject
	GUI gui;
	
	ClipboardController() {
		DaggerIOC.getAppComponent().inject(this);
	}
	
	public void copySelectedItems(boolean info) {
		if (treeManager.selectionManager().isSelectionMode()) {
			treeManager.clipboardManager().clearClipboard();
			for (Integer selectedItemId : treeManager.selectionManager().getSelectedItems()) {
				TreeItem selectedItem = treeManager.getCurrentItem().getChild(selectedItemId);
				treeManager.clipboardManager().addToClipboard(selectedItem);
			}
			//jeśli zaznaczony jeden element - skopiowanie do schowka
			if (treeManager.clipboardManager().getClipboardSize() == 1) {
				TreeItem item = treeManager.clipboardManager().getClipboard().get(0);
				clipboardManager.copyToSystemClipboard(item.getContent());
				if (info) {
					userInfo.showInfo("Item copied: " + item.getContent());
				}
			} else {
				if (info) {
					userInfo.showInfo("Selected items copied: " + treeManager.clipboardManager()
							.getClipboardSize());
				}
			}
		}
	}
	
	
	public void cutSelectedItems() {
		if (treeManager.selectionManager().isSelectionMode() && treeManager.selectionManager()
				.getSelectedItemsCount() > 0) {
			copySelectedItems(false);
			userInfo.showInfo("Selected items cut: " + treeManager.selectionManager()
					.getSelectedItemsCount());
			new ItemTrashController().removeSelectedItems(false);
		} else {
			userInfo.showInfo("No selected items");
		}
	}
	
	public void pasteItems() {
		if (treeManager.clipboardManager().isClipboardEmpty()) {
			String systemClipboard = clipboardManager.getSystemClipboard();
			if (systemClipboard != null) {
				//wklejanie 1 elementu z systemowego schowka
				treeManager.getCurrentItem().add(systemClipboard);
				userInfo.showInfo("Item pasted: " + systemClipboard);
				new GUIController().updateItemsList();
				gui.scrollToItem(-1);
			} else {
				userInfo.showInfo("Clipboard is empty.");
			}
		} else {
			for (TreeItem clipboardItem : treeManager.clipboardManager().getClipboard()) {
				clipboardItem.setParent(treeManager.getCurrentItem());
				treeManager.addToCurrent(clipboardItem);
			}
			userInfo.showInfo("Items pasted: " + treeManager.clipboardManager().getClipboardSize());
			treeManager.clipboardManager().recopyClipboard();
			new GUIController().updateItemsList();
			gui.scrollToItem(-1);
		}
	}
}
