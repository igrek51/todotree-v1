package igrek.todotree.controller;


import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import igrek.todotree.R;
import igrek.todotree.app.App;
import igrek.todotree.app.AppData;
import igrek.todotree.app.AppState;
import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.datatree.ContentTrimmer;
import igrek.todotree.datatree.TreeManager;
import igrek.todotree.datatree.item.TreeItem;
import igrek.todotree.exceptions.NoSuperItemException;
import igrek.todotree.gui.GUI;
import igrek.todotree.logger.Logs;
import igrek.todotree.services.clipboard.ClipboardManager;
import igrek.todotree.services.lock.DatabaseLock;
import igrek.todotree.services.resources.InfoBarClickAction;
import igrek.todotree.services.resources.UserInfoService;

//TODO brak zapisu bazy jeśli nie było zmian
//TODO: funkcja cofania zmian - zapisywanie modyfikacji, dodawania, usuwania elementów, przesuwania
// TODO split responsibilities to multiple services

public class MainController {
	
	@Inject
	TreeManager treeManager;
	
	@Inject
	GUI gui;
	
	@Inject
	UserInfoService userInfo;
	
	@Inject
	ClipboardManager clipboardManager;
	
	@Inject
	App app;
	
	@Inject
	AppData appData;
	
	@Inject
	DatabaseLock lock;
	
	@Inject
	ContentTrimmer contentTrimmer;
	
	public MainController() {
		DaggerIOC.getAppComponent().inject(this);
	}
	
	public boolean optionsSelect(int id) {
		if (id == R.id.action_minimize) {
			app.minimize();
			return true;
		} else if (id == R.id.action_exit_without_saving) {
			new ExitController().exitApp();
			return true;
		} else if (id == R.id.action_save_exit) {
			new ExitController().optionSaveAndExit();
			return true;
		} else if (id == R.id.action_save) {
			new PersistenceController().optionSave();
			return true;
		} else if (id == R.id.action_reload) {
			new PersistenceController().optionReload();
			return true;
		} else if (id == R.id.action_copy) {
			new ClipboardController().copySelectedItems(true);
		} else if (id == R.id.action_cut) {
			new ClipboardController().cutSelectedItems();
		} else if (id == R.id.action_paste) {
			new ClipboardController().pasteItems();
		} else if (id == R.id.action_select_all) {
			toggleSelectAll();
		} else if (id == R.id.action_sum_selected) {
			sumSelected();
		}
		return false;
	}
	
	
	public void updateItemsList() {
		gui.updateItemsList(treeManager.getCurrentItem(), treeManager.selectionManager()
				.getSelectedItems());
		appData.setState(AppState.ITEMS_LIST);
	}
	
	
	/**
	 * @param position pozycja nowego elementu (0 - początek, ujemna wartość - na końcu listy)
	 */
	private void newItem(int position) {
		if (position < 0)
			position = treeManager.getCurrentItem().size();
		if (position > treeManager.getCurrentItem().size())
			position = treeManager.getCurrentItem().size();
		treeManager.scrollStore()
				.storeScrollPosition(treeManager.getCurrentItem(), gui.getCurrentScrollPos());
		treeManager.setNewItemPosition(position);
		gui.showEditItemPanel(null, treeManager.getCurrentItem());
		appData.setState(AppState.EDIT_ITEM_CONTENT);
	}
	
	private void editItem(TreeItem item, TreeItem parent) {
		treeManager.scrollStore()
				.storeScrollPosition(treeManager.getCurrentItem(), gui.getCurrentScrollPos());
		treeManager.setNewItemPosition(null);
		gui.showEditItemPanel(item, parent);
		appData.setState(AppState.EDIT_ITEM_CONTENT);
	}
	
	private void discardEditingItem() {
		returnFromItemEditing();
		userInfo.showInfo("Editing item cancelled.");
	}
	
	private void removeItem(final int position) {
		
		final TreeItem removing = treeManager.getCurrentItem().getChild(position);
		
		treeManager.getCurrentItem().remove(position);
		updateItemsList();
		userInfo.showInfoCancellable("Item removed: " + removing.getContent(), new InfoBarClickAction() {
			@Override
			public void onClick() {
				restoreRemovedItem(removing, position);
			}
		});
	}
	
	private void restoreRemovedItem(TreeItem restored, int position) {
		treeManager.getCurrentItem().add(position, restored);
		showItemsList();
		gui.scrollToItem(position);
		userInfo.showInfo("Removed item restored.");
	}
	
	public void removeSelectedItems(boolean info) {
		List<Integer> selectedIds = treeManager.selectionManager().getSelectedItems();
		//posortowanie malejąco (żeby przy usuwaniu nie nadpisać indeksów)
		Collections.sort(selectedIds, new Comparator<Integer>() {
			@Override
			public int compare(Integer lhs, Integer rhs) {
				return rhs.compareTo(lhs);
			}
		});
		for (Integer id : selectedIds) {
			treeManager.getCurrentItem().remove(id);
		}
		if (info) {
			userInfo.showInfo("Selected items removed: " + selectedIds.size());
		}
		treeManager.selectionManager().cancelSelectionMode();
		updateItemsList();
	}
	
	private void goUp() {
		try {
			TreeItem current = treeManager.getCurrentItem();
			TreeItem parent = current.getParent();
			treeManager.goUp();
			updateItemsList();
			restoreScrollPosition(parent);
		} catch (NoSuperItemException e) {
			new ExitController().saveAndExitRequested();
		}
	}
	
	private void restoreScrollPosition(TreeItem parent) {
		Integer savedScrollPos = treeManager.scrollStore().restoreScrollPosition(parent);
		if (savedScrollPos != null) {
			gui.scrollToPosition(savedScrollPos);
		}
	}
	
	public void backClicked() {
		if (appData.isState(AppState.ITEMS_LIST)) {
			if (treeManager.selectionManager().isSelectionMode()) {
				treeManager.selectionManager().cancelSelectionMode();
				updateItemsList();
			} else {
				goUp();
			}
		} else if (appData.isState(AppState.EDIT_ITEM_CONTENT)) {
			if (gui.editItemBackClicked())
				return;
			cancelEditedItem();
		}
	}
	
	
	private void selectAllItems(boolean selectedState) {
		for (int i = 0; i < treeManager.getCurrentItem().size(); i++) {
			treeManager.selectionManager().setItemSelected(i, selectedState);
		}
	}
	
	private void toggleSelectAll() {
		if (treeManager.selectionManager().getSelectedItemsCount() == treeManager.getCurrentItem()
				.size()) {
			treeManager.selectionManager().cancelSelectionMode();
		} else {
			selectAllItems(true);
		}
		updateItemsList();
	}
	
	
	private void sumSelected() {
		if (treeManager.selectionManager().isSelectionMode()) {
			try {
				BigDecimal sum = treeManager.sumSelected();
				
				String clipboardStr = sum.toPlainString();
				clipboardStr = clipboardStr.replace('.', ',');
				
				clipboardManager.copyToSystemClipboard(clipboardStr);
				userInfo.showInfo("Sum copied to clipboard: " + clipboardStr);
				
			} catch (NumberFormatException e) {
				userInfo.showInfo(e.getMessage());
			}
		}
	}
	
	public void itemMoved(int position, int step) {
		treeManager.mover().move(treeManager.getCurrentItem(), position, step);
	}
	
	public void selectedItemClicked(int position, boolean checked) {
		treeManager.selectionManager().setItemSelected(position, checked);
		updateItemsList();
	}
	
	
	private boolean tryToSaveNewItem(String content) {
		content = contentTrimmer.trimContent(content);
		if (content.isEmpty()) {
			userInfo.showInfo("Empty item has been removed.");
			return false;
		} else {
			treeManager.addToCurrent(treeManager.getNewItemPosition(), content);
			userInfo.showInfo("New item has been saved.");
			return true;
		}
	}
	
	private boolean tryToSaveExistingItem(TreeItem editedItem, String content) {
		content = contentTrimmer.trimContent(content);
		if (content.isEmpty()) {
			treeManager.getCurrentItem().remove(editedItem);
			userInfo.showInfo("Empty item has been removed.");
			return false;
		} else {
			editedItem.setContent(content);
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
		showItemsList();
		if (treeManager.getNewItemPosition() != null) { //editing existing item
			if (treeManager.getNewItemPosition() == treeManager.getCurrentItem()
					.size() - 1) { // last item
				gui.scrollToBottom();
			} else {
				restoreScrollPosition(treeManager.getCurrentItem());
			}
		}
		treeManager.setNewItemPosition(null);
	}
	
	public void saveItem(TreeItem editedItem, String content) {
		tryToSaveItem(editedItem, content);
		returnFromItemEditing();
	}
	
	public void saveAndGoIntoItemClicked(TreeItem editedItem, String content) {
		if (!tryToSaveItem(editedItem, content)) {
			returnFromItemEditing();
			return;
		}
		// go into
		Integer editedItemIndex = treeManager.getNewItemPosition();
		if (editedItemIndex != null) {
			//wejście wewnątrz
			treeManager.goInto(editedItemIndex, gui.getCurrentScrollPos());
			//dodawanie nowego elementu na końcu
			newItem(-1);
		}
	}
	
	public void saveAndAddItemClicked(TreeItem editedItem, String content) {
		int newItemIndex = editedItem == null ? treeManager.getNewItemPosition() : editedItem.getIndexInParent();
		if (!tryToSaveItem(editedItem, content)) {
			returnFromItemEditing();
			return;
		}
		// add new after
		newItem(newItemIndex + 1);
	}
	
	
	private void showItemsList() {
		appData.setState(AppState.ITEMS_LIST);
		gui.showItemsList(treeManager.getCurrentItem());
	}
	
	
	public void itemRemoveClicked(int position) {// removing locked before going into first element
		if (lock.isLocked()) {
			Logs.warn("Database is locked.");
		} else {
			if (treeManager.selectionManager().isSelectionMode()) {
				removeSelectedItems(true);
			} else {
				removeItem(position);
			}
		}
	}
	
	public void itemLongClicked(int position) {
		if (!treeManager.selectionManager().isSelectionMode()) {
			treeManager.selectionManager().startSelectionMode();
			treeManager.selectionManager().setItemSelected(position, true);
			updateItemsList();
			gui.scrollToItem(position);
		} else {
			treeManager.selectionManager().setItemSelected(position, true);
			updateItemsList();
		}
	}
	
	public void itemGoIntoClicked(int position) {
		if (lock.isLocked()) {
			lock.setLocked(false);
			Logs.debug("Database unlocked.");
		}
		treeManager.selectionManager().cancelSelectionMode();
		treeManager.goInto(position, gui.getCurrentScrollPos());
		updateItemsList();
		gui.scrollToItem(0);
	}
	
	public void itemEditClicked(TreeItem item) {
		treeManager.selectionManager().cancelSelectionMode();
		editItem(item, treeManager.getCurrentItem());
	}
	
	public void itemClicked(int position, TreeItem item) {
		// blokada wejścia wgłąb na pierwszym poziomie poprzez kliknięcie
		if (lock.isLocked()) {
			Logs.warn("Database is locked.");
			return;
		}
		if (treeManager.selectionManager().isSelectionMode()) {
			treeManager.selectionManager().toggleItemSelected(position);
			updateItemsList();
		} else {
			if (item.isEmpty()) {
				itemEditClicked(item);
			} else {
				itemGoIntoClicked(position);
			}
		}
	}
	
	public void cancelEditedItem() {
		gui.hideSoftKeyboard();
		discardEditingItem();
	}
	
	public void addItemClickedPos(int position) {
		treeManager.selectionManager().cancelSelectionMode();
		newItem(position);
	}
	
	public void addItemClicked() {
		addItemClickedPos(-1);
	}
	
}
