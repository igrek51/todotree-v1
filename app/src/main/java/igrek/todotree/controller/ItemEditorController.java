package igrek.todotree.controller;


import javax.inject.Inject;

import igrek.todotree.app.AppData;
import igrek.todotree.app.AppState;
import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.datatree.ContentTrimmer;
import igrek.todotree.datatree.TreeManager;
import igrek.todotree.datatree.TreeScrollCache;
import igrek.todotree.datatree.TreeSelectionManager;
import igrek.todotree.datatree.item.TreeItem;
import igrek.todotree.services.history.ChangesHistory;
import igrek.todotree.services.lock.DatabaseLock;
import igrek.todotree.services.resources.UserInfoService;
import igrek.todotree.ui.GUI;

public class ItemEditorController {
	
	@Inject
	TreeManager treeManager;
	
	@Inject
	GUI gui;
	
	@Inject
	UserInfoService userInfo;
	
	@Inject
	ContentTrimmer contentTrimmer;
	
	@Inject
	AppData appData;
	
	@Inject
	TreeScrollCache scrollCache;
	
	@Inject
	TreeSelectionManager selectionManager;
	
	@Inject
	DatabaseLock lock;
	
	@Inject
	ChangesHistory changesHistory;
	
	public ItemEditorController() {
		DaggerIOC.getAppComponent().inject(this);
	}
	
	private Integer getNewItemPosition() {
		return treeManager.getNewItemPosition();
	}
	
	private boolean tryToSaveNewItem(String content) {
		content = contentTrimmer.trimContent(content);
		if (content.isEmpty()) {
			userInfo.showInfo("Empty item has been removed.");
			return false;
		} else {
			treeManager.addToCurrent(getNewItemPosition(), content);
			userInfo.showInfo("New item has been saved.");
			return true;
		}
	}
	
	private boolean tryToSaveExistingItem(TreeItem editedItem, String content) {
		content = contentTrimmer.trimContent(content);
		if (content.isEmpty()) {
			treeManager.removeFromCurrent(editedItem);
			userInfo.showInfo("Empty item has been removed.");
			return false;
		} else {
			editedItem.setContent(content);
			changesHistory.registerChange();
			userInfo.showInfo("Item has been saved.");
			return true;
		}
	}
	
	private boolean tryToSaveItem(TreeItem editedItem, String content) {
		if (editedItem == null) { // new item
			return tryToSaveNewItem(content);
		} else { // existing item
			return tryToSaveExistingItem(editedItem, content);
		}
	}
	
	private void returnFromItemEditing() {
		new GUIController().showItemsList();
		if (getNewItemPosition() != null) { //editing existing item
			if (getNewItemPosition() == treeManager.getCurrentItem().size() - 1) { // last item
				gui.scrollToBottom();
			} else {
				new GUIController().restoreScrollPosition(treeManager.getCurrentItem());
			}
		}
		treeManager.setNewItemPosition(null);
	}
	
	public void saveItem(TreeItem editedItem, String content) {
		tryToSaveItem(editedItem, content);
		returnFromItemEditing();
	}
	
	public void saveAndAddItemClicked(TreeItem editedItem, String content) {
		int newItemIndex = editedItem == null ? getNewItemPosition() : editedItem.getIndexInParent();
		if (!tryToSaveItem(editedItem, content)) {
			returnFromItemEditing();
			return;
		}
		// add new after
		newItem(newItemIndex + 1);
	}
	
	public void saveAndGoIntoItemClicked(TreeItem editedItem, String content) {
		if (!tryToSaveItem(editedItem, content)) {
			returnFromItemEditing();
			return;
		}
		// go into
		Integer editedItemIndex = getNewItemPosition();
		if (editedItemIndex != null) {
			new TreeController().goInto(editedItemIndex);
			newItem(-1);
		}
	}
	
	/**
	 * @param position posistion of new element (0 - begginning, negative value - in the end of list)
	 */
	private void newItem(int position) {
		if (position < 0)
			position = treeManager.getCurrentItem().size();
		if (position > treeManager.getCurrentItem().size())
			position = treeManager.getCurrentItem().size();
		scrollCache.storeScrollPosition(treeManager.getCurrentItem(), gui.getCurrentScrollPos());
		treeManager.setNewItemPosition(position);
		gui.showEditItemPanel(null, treeManager.getCurrentItem());
		appData.setState(AppState.EDIT_ITEM_CONTENT);
	}
	
	private void editItem(TreeItem item, TreeItem parent) {
		scrollCache.storeScrollPosition(treeManager.getCurrentItem(), gui.getCurrentScrollPos());
		treeManager.setNewItemPosition(null);
		gui.showEditItemPanel(item, parent);
		appData.setState(AppState.EDIT_ITEM_CONTENT);
	}
	
	private void discardEditingItem() {
		returnFromItemEditing();
		userInfo.showInfo("Editing item cancelled.");
	}
	
	public void itemEditClicked(TreeItem item) {
		selectionManager.cancelSelectionMode();
		editItem(item, treeManager.getCurrentItem());
	}
	
	public void cancelEditedItem() {
		gui.hideSoftKeyboard();
		discardEditingItem();
	}
	
	public void addItemHereClicked(int position) {
		selectionManager.cancelSelectionMode();
		newItem(position);
	}
	
	public void addItemClicked() {
		addItemHereClicked(-1);
	}
}
