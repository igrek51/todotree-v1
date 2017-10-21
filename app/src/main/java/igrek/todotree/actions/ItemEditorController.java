package igrek.todotree.actions;


import javax.inject.Inject;

import igrek.todotree.app.AppData;
import igrek.todotree.app.AppState;
import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.logger.Logs;
import igrek.todotree.model.treeitem.AbstractTreeItem;
import igrek.todotree.model.treeitem.LinkTreeItem;
import igrek.todotree.model.treeitem.TextTreeItem;
import igrek.todotree.services.history.ChangesHistory;
import igrek.todotree.services.lock.DatabaseLock;
import igrek.todotree.services.resources.UserInfoService;
import igrek.todotree.services.tree.ContentTrimmer;
import igrek.todotree.services.tree.TreeManager;
import igrek.todotree.services.tree.TreeScrollCache;
import igrek.todotree.services.tree.TreeSelectionManager;
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
	
	@Inject
	Logs logger;
	
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
			treeManager.addToCurrent(getNewItemPosition(), new TextTreeItem(content));
			userInfo.showInfo("New item has been saved.");
			return true;
		}
	}
	
	private boolean tryToSaveExistingItem(TextTreeItem editedItem, String content) {
		content = contentTrimmer.trimContent(content);
		if (content.isEmpty()) {
			treeManager.removeFromCurrent(editedItem);
			userInfo.showInfo("Empty item has been removed.");
			return false;
		} else {
			editedItem.setName(content);
			changesHistory.registerChange();
			userInfo.showInfo("Item has been saved.");
			return true;
		}
	}
	
	private boolean tryToSaveExistingLink(LinkTreeItem editedLinkItem, String content) {
		content = contentTrimmer.trimContent(content);
		if (content.isEmpty()) {
			treeManager.removeFromCurrent(editedLinkItem);
			userInfo.showInfo("Empty link has been removed.");
			return false;
		} else {
			editedLinkItem.setCustomName(content);
			changesHistory.registerChange();
			userInfo.showInfo("Link name has been saved.");
			return true;
		}
	}
	
	private boolean tryToSaveItem(AbstractTreeItem editedItem, String content) {
		if (editedItem == null) { // new item
			return tryToSaveNewItem(content);
		} else { // existing item
			if (editedItem instanceof TextTreeItem) {
				return tryToSaveExistingItem((TextTreeItem) editedItem, content);
			} else if (editedItem instanceof LinkTreeItem) {
				return tryToSaveExistingLink((LinkTreeItem) editedItem, content);
			} else {
				logger.warn("trying to save item of type: " + editedItem.getTypeName());
				return false;
			}
		}
	}
	
	private void returnFromItemEditing() {
		new GUIController().showItemsList();
		// when new item has been added to the end
		if (getNewItemPosition() != null && getNewItemPosition() == treeManager.getCurrentItem()
				.size() - 1) {
			gui.scrollToBottom();
		} else {
			new GUIController().restoreScrollPosition(treeManager.getCurrentItem());
		}
		treeManager.setNewItemPosition(null);
	}
	
	public void saveItem(AbstractTreeItem editedItem, String content) {
		tryToSaveItem(editedItem, content);
		returnFromItemEditing();
	}
	
	public void saveAndAddItemClicked(AbstractTreeItem editedItem, String content) {
		int newItemIndex = editedItem == null ? getNewItemPosition() : editedItem.getIndexInParent();
		if (!tryToSaveItem(editedItem, content)) {
			returnFromItemEditing();
			return;
		}
		// add new after
		newItem(newItemIndex + 1);
	}
	
	public void saveAndGoIntoItemClicked(AbstractTreeItem editedItem, String content) {
		if (!tryToSaveItem(editedItem, content)) {
			returnFromItemEditing();
			return;
		}
		// go into
		Integer editedItemIndex = getNewItemPosition();
		if (editedItemIndex == null) {
			editedItemIndex = editedItem.getIndexInParent();
		}
		new TreeController().goInto(editedItemIndex);
		newItem(-1);
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
	
	private void editItem(AbstractTreeItem item, AbstractTreeItem parent) {
		scrollCache.storeScrollPosition(treeManager.getCurrentItem(), gui.getCurrentScrollPos());
		treeManager.setNewItemPosition(null);
		gui.showEditItemPanel(item, parent);
		appData.setState(AppState.EDIT_ITEM_CONTENT);
	}
	
	private void discardEditingItem() {
		returnFromItemEditing();
		userInfo.showInfo("Editing item cancelled.");
	}
	
	public void itemEditClicked(AbstractTreeItem item) {
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
