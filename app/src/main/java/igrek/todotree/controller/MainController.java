package igrek.todotree.controller;


import android.os.Handler;
import android.view.View;
import android.view.ViewTreeObserver;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import igrek.todotree.R;
import igrek.todotree.app.App;
import igrek.todotree.app.AppData;
import igrek.todotree.app.AppState;
import igrek.todotree.exceptions.NoSuperItemException;
import igrek.todotree.gui.GUI;
import igrek.todotree.logger.Logs;
import igrek.todotree.services.backup.BackupManager;
import igrek.todotree.services.clipboard.ClipboardManager;
import igrek.todotree.services.datatree.TreeItem;
import igrek.todotree.services.datatree.TreeManager;
import igrek.todotree.services.lock.DatabaseLock;
import igrek.todotree.services.preferences.Preferences;
import igrek.todotree.services.resources.InfoBarClickAction;
import igrek.todotree.services.resources.UserInfoService;

//TODO brak zapisu bazy jeśli nie było zmian
//TODO: funkcja cofania zmian - zapisywanie modyfikacji, dodawania, usuwania elementów, przesuwania

public class MainController {
	
	private TreeManager treeManager;
	private BackupManager backupManager;
	private GUI gui;
	private UserInfoService userInfo;
	private ClipboardManager clipboardManager;
	private Preferences preferences;
	private App app;
	private AppData appData;
	private DatabaseLock lock;
	
	public MainController(TreeManager treeManager, BackupManager backupManager, GUI gui, UserInfoService userInfo, ClipboardManager clipboardManager, Preferences preferences, App app, AppData appData, DatabaseLock lock) {
		// TODO split responsibilities to multiple services
		this.treeManager = treeManager;
		this.backupManager = backupManager;
		this.gui = gui;
		this.userInfo = userInfo;
		this.clipboardManager = clipboardManager;
		this.preferences = preferences;
		this.app = app;
		this.appData = appData;
		this.lock = lock;
	}
	
	public boolean optionsSelect(int id) {
		if (id == R.id.action_minimize) {
			app.minimize();
			return true;
		} else if (id == R.id.action_exit_without_saving) {
			exitApp(false);
			return true;
		} else if (id == R.id.action_save_exit) {
			optionSaveAndExit();
			return true;
		} else if (id == R.id.action_save) {
			optionSave();
			return true;
		} else if (id == R.id.action_reload) {
			optionReload();
			return true;
		} else if (id == R.id.action_copy) {
			copySelectedItems(true);
		} else if (id == R.id.action_cut) {
			cutSelectedItems();
		} else if (id == R.id.action_paste) {
			pasteItems();
		} else if (id == R.id.action_select_all) {
			toggleSelectAll();
		} else if (id == R.id.action_sum_selected) {
			sumSelected();
		}
		return false;
	}
	
	private void optionReload() {
		treeManager.reset();
		treeManager.loadRootTree();
		updateItemsList();
		userInfo.showInfo("Database loaded.");
	}
	
	private void optionSave() {
		saveDatabase();
		userInfo.showInfo("Database saved.");
	}
	
	private void optionSaveAndExit() {
		if (appData.getState() == AppState.EDIT_ITEM_CONTENT) {
			gui.requestSaveEditedItem();
		}
		exitApp(true);
	}
	
	private void saveDatabase() {
		treeManager.saveRootTree();
		backupManager.saveBackupFile();
	}
	
	private void updateItemsList() {
		gui.updateItemsList(treeManager.getCurrentItem(), treeManager.getSelectedItems());
		appData.setState(AppState.ITEMS_LIST);
	}
	
	private void exitApp(final boolean withSaving) {
		// show exit screen and wait for rendered
		View exitScreen = gui.showExitScreen();
		
		final ViewTreeObserver vto = exitScreen.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				new Handler().post(new Runnable() {
					@Override
					public void run() {
						saveAndExit(withSaving);
					}
				});
			}
		});
	}
	
	private void saveAndExit(boolean withSaving) {
		if (withSaving) {
			saveDatabase();
		}
		exitAppRequested();
	}
	
	
	/**
	 * @param position pozycja nowego elementu (0 - początek, ujemna wartość - na końcu listy)
	 */
	private void newItem(int position) {
		if (position < 0)
			position = treeManager.getCurrentItem().size();
		if (position > treeManager.getCurrentItem().size())
			position = treeManager.getCurrentItem().size();
		treeManager.storeScrollPosition(treeManager.getCurrentItem(), gui.getCurrentScrollPos());
		treeManager.setNewItemPosition(position);
		gui.showEditItemPanel(null, treeManager.getCurrentItem());
		appData.setState(AppState.EDIT_ITEM_CONTENT);
	}
	
	private void editItem(TreeItem item, TreeItem parent) {
		treeManager.storeScrollPosition(treeManager.getCurrentItem(), gui.getCurrentScrollPos());
		treeManager.setEditItem();
		gui.showEditItemPanel(item, parent);
		appData.setState(AppState.EDIT_ITEM_CONTENT);
	}
	
	private void discardEditingItem() {
		treeManager.setEditItem();
		appData.setState(AppState.ITEMS_LIST);
		gui.showItemsList(treeManager.getCurrentItem());
		restoreScrollPosition(treeManager.getCurrentItem());
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
		appData.setState(AppState.ITEMS_LIST);
		gui.showItemsList(treeManager.getCurrentItem());
		gui.scrollToItem(position);
		userInfo.showInfo("Removed item restored.");
	}
	
	private void removeSelectedItems(boolean info) {
		List<Integer> selectedIds = treeManager.getSelectedItems();
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
		treeManager.cancelSelectionMode();
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
			exitApp(true);
		}
	}
	
	private void restoreScrollPosition(TreeItem parent) {
		Integer savedScrollPos = treeManager.restoreScrollPosition(parent);
		if (savedScrollPos != null) {
			gui.scrollToPosition(savedScrollPos);
		}
	}
	
	public void backClicked() {
		if (appData.getState() == AppState.ITEMS_LIST) {
			if (treeManager.isSelectionMode()) {
				treeManager.cancelSelectionMode();
				updateItemsList();
			} else {
				goUp();
			}
		} else if (appData.getState() == AppState.EDIT_ITEM_CONTENT) {
			if (gui.editItemBackClicked())
				return;
			discardEditingItem();
		}
	}
	
	private void copySelectedItems(boolean info) {
		if (treeManager.isSelectionMode()) {
			treeManager.getTreeClipboardManager().clearClipboard();
			for (Integer selectedItemId : treeManager.getSelectedItems()) {
				TreeItem selectedItem = treeManager.getCurrentItem().getChild(selectedItemId);
				treeManager.getTreeClipboardManager().addToClipboard(selectedItem);
			}
			//jeśli zaznaczony jeden element - skopiowanie do schowka
			if (treeManager.getTreeClipboardManager().getClipboardSize() == 1) {
				TreeItem item = treeManager.getTreeClipboardManager().getClipboard().get(0);
				clipboardManager.copyToSystemClipboard(item.getContent());
				if (info) {
					userInfo.showInfo("Item copied: " + item.getContent());
				}
			} else {
				if (info) {
					userInfo.showInfo("Selected items copied: " + treeManager.getTreeClipboardManager()
							.getClipboardSize());
				}
			}
		}
	}
	
	private void cutSelectedItems() {
		if (treeManager.isSelectionMode() && treeManager.getSelectedItemsCount() > 0) {
			copySelectedItems(false);
			userInfo.showInfo("Selected items cut: " + treeManager.getSelectedItemsCount());
			removeSelectedItems(false);
		} else {
			userInfo.showInfo("No selected items");
		}
	}
	
	private void pasteItems() {
		if (treeManager.getTreeClipboardManager().isClipboardEmpty()) {
			String systemClipboard = clipboardManager.getSystemClipboard();
			if (systemClipboard != null) {
				//wklejanie 1 elementu z systemowego schowka
				treeManager.getCurrentItem().add(systemClipboard);
				userInfo.showInfo("Item pasted: " + systemClipboard);
				updateItemsList();
				gui.scrollToItem(-1);
			} else {
				userInfo.showInfo("Clipboard is empty.");
			}
		} else {
			for (TreeItem clipboardItem : treeManager.getTreeClipboardManager().getClipboard()) {
				clipboardItem.setParent(treeManager.getCurrentItem());
				treeManager.addToCurrent(clipboardItem);
			}
			userInfo.showInfo("Items pasted: " + treeManager.getTreeClipboardManager()
					.getClipboardSize());
			treeManager.getTreeClipboardManager().recopyClipboard();
			updateItemsList();
			gui.scrollToItem(-1);
		}
	}
	
	private void selectAllItems(boolean selectedState) {
		for (int i = 0; i < treeManager.getCurrentItem().size(); i++) {
			treeManager.setItemSelected(i, selectedState);
		}
	}
	
	private void toggleSelectAll() {
		if (treeManager.getSelectedItemsCount() == treeManager.getCurrentItem().size()) {
			treeManager.cancelSelectionMode();
		} else {
			selectAllItems(true);
		}
		updateItemsList();
	}
	
	
	private void sumSelected() {
		if (treeManager.isSelectionMode()) {
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
	
	
	private void exitAppRequested() {
		preferences.saveAll();
		app.quit();
	}
	
	public void itemMoved(int position, int step) {
		treeManager.move(treeManager.getCurrentItem(), position, step);
	}
	
	public void selectedItemClicked(int position, boolean checked) {
		treeManager.setItemSelected(position, checked);
		updateItemsList();
	}
	
	public void newItemSaved(String content) {
		content = treeManager.trimContent(content);
		if (content.isEmpty()) {
			userInfo.showInfo("Empty item has been removed.");
		} else {
			treeManager.getCurrentItem().add(treeManager.getNewItemPosition(), content);
			userInfo.showInfo("New item has been saved.");
		}
		appData.setState(AppState.ITEMS_LIST);
		gui.showItemsList(treeManager.getCurrentItem());
		if (treeManager.getNewItemPosition() == treeManager.getCurrentItem().size() - 1) {
			gui.scrollToBottom();
		} else {
			restoreScrollPosition(treeManager.getCurrentItem());
		}
		treeManager.setNewItemPosition(null);
	}
	
	public void editedItemSaved(TreeItem editedItem, String content) {
		content = treeManager.trimContent(content);
		if (content.isEmpty()) {
			// TODO repeatable code
			treeManager.getCurrentItem().remove(editedItem);
			userInfo.showInfo("Empty item has been removed.");
		} else {
			editedItem.setContent(content);
			userInfo.showInfo("Item has been saved.");
		}
		treeManager.setEditItem();
		appData.setState(AppState.ITEMS_LIST);
		gui.showItemsList(treeManager.getCurrentItem());
		restoreScrollPosition(treeManager.getCurrentItem());
	}
	
	public void saveAndGoIntoItemClicked(TreeItem editedItem, String content) {
		//zapis
		content = treeManager.trimContent(content);
		Integer editedItemIndex = null;
		if (editedItem == null) { //nowy element
			if (content.isEmpty()) {
				// repeatable code
				treeManager.setEditItem();
				appData.setState(AppState.ITEMS_LIST);
				gui.showItemsList(treeManager.getCurrentItem());
				restoreScrollPosition(treeManager.getCurrentItem());
				userInfo.showInfo("Empty item has been removed.");
			} else {
				editedItemIndex = treeManager.getNewItemPosition();
				treeManager.getCurrentItem().add(treeManager.getNewItemPosition(), content);
				userInfo.showInfo("New item has been saved.");
			}
		} else { //edycja
			if (content.isEmpty()) {
				treeManager.getCurrentItem().remove(editedItem);
				treeManager.setEditItem();
				appData.setState(AppState.ITEMS_LIST);
				gui.showItemsList(treeManager.getCurrentItem());
				restoreScrollPosition(treeManager.getCurrentItem());
				userInfo.showInfo("Empty item has been removed.");
			} else {
				editedItemIndex = editedItem.getIndexInParent();
				editedItem.setContent(content);
				userInfo.showInfo("Item has been saved.");
			}
		}
		if (editedItemIndex != null) {
			//wejście wewnątrz
			treeManager.goInto(editedItemIndex, gui.getCurrentScrollPos());
			//dodawanie nowego elementu na końcu
			newItem(-1);
		}
	}
	
	public void saveAndAddItemClicked(TreeItem editedItem, String content) {
		//zapis
		content = treeManager.trimContent(content);
		int newItemIndex;
		if (editedItem == null) { //nowy element
			newItemIndex = treeManager.getNewItemPosition();
			if (content.isEmpty()) {
				// TODO repeatable code
				treeManager.setEditItem();
				appData.setState(AppState.ITEMS_LIST);
				gui.showItemsList(treeManager.getCurrentItem());
				restoreScrollPosition(treeManager.getCurrentItem());
				userInfo.showInfo("Empty item has been removed.");
				return;
			} else {
				treeManager.getCurrentItem().add(treeManager.getNewItemPosition(), content);
				newItemIndex++;
				userInfo.showInfo("New item has been saved.");
			}
		} else { //edycja
			newItemIndex = editedItem.getIndexInParent();
			if (content.isEmpty()) {
				// TODO repeatable code
				treeManager.getCurrentItem().remove(editedItem);
				treeManager.setEditItem();
				appData.setState(AppState.ITEMS_LIST);
				gui.showItemsList(treeManager.getCurrentItem());
				restoreScrollPosition(treeManager.getCurrentItem());
				userInfo.showInfo("Empty item has been removed.");
				return;
			} else {
				editedItem.setContent(content);
				newItemIndex++;
				userInfo.showInfo("Item has been saved.");
			}
		}
		//dodanie nowego elementu
		newItem(newItemIndex);
	}
	
	public void itemRemoveClicked(int position) {// removing locked before going into first element
		if (lock.isLocked()) {
			Logs.warn("Database is locked.");
		} else {
			if (treeManager.isSelectionMode()) {
				removeSelectedItems(true);
			} else {
				removeItem(position);
			}
		}
	}
	
	public void itemLongClicked(int position) {
		if (!treeManager.isSelectionMode()) {
			treeManager.startSelectionMode();
			treeManager.setItemSelected(position, true);
			updateItemsList();
			gui.scrollToItem(position);
		} else {
			treeManager.setItemSelected(position, true);
			updateItemsList();
		}
	}
	
	public void itemGoIntoClicked(int position) {
		if (lock.isLocked()) {
			lock.setLocked(false);
			Logs.debug("Database unlocked.");
		}
		treeManager.cancelSelectionMode();
		treeManager.goInto(position, gui.getCurrentScrollPos());
		updateItemsList();
		gui.scrollToItem(0);
	}
	
	public void itemEditClicked(TreeItem item) {
		treeManager.cancelSelectionMode();
		editItem(item, treeManager.getCurrentItem());
	}
	
	public void itemClicked(int position, TreeItem item) {
		// blokada wejścia wgłąb na pierwszym poziomie poprzez kliknięcie
		if (lock.isLocked()) {
			Logs.warn("Database is locked.");
			return;
		}
		if (treeManager.isSelectionMode()) {
			treeManager.toggleItemSelected(position);
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
		treeManager.cancelSelectionMode();
		newItem(position);
	}
	
	public void addItemClicked() {
		addItemClickedPos(-1);
	}
	
	public void toolbarBackClicked() {
		backClicked();
	}
	
}
