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
import igrek.todotree.services.resources.resources.InfoBarClickAction;
import igrek.todotree.services.resources.resources.UserInfoService;

//TODO brak zapisu bazy jeśli nie było zmian

//TODO: funkcja cofania zmian - zapisywanie modyfikacji, dodawania, usuwania elementów, przesuwania

//TODO SERWISY: do blokowania bazy, do pobierania treści komunikatów, stan aplikacji

public class LogicActionController {
	
	private TreeManager treeManager;
	private BackupManager backupManager;
	private GUI gui;
	private UserInfoService userInfo;
	private ClipboardManager clipboardManager;
	private Preferences preferences;
	private App app;
	private AppData appData;
	private DatabaseLock lock;
	
	public LogicActionController(TreeManager treeManager, BackupManager backupManager, GUI gui, UserInfoService userInfo, ClipboardManager clipboardManager, Preferences preferences, App app, AppData appData, DatabaseLock lock) {
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
			if (appData.getState() == AppState.EDIT_ITEM_CONTENT) {
				gui.requestSaveEditedItem();
			}
			exitApp(true);
			return true;
		} else if (id == R.id.action_save) {
			saveDatabase();
			userInfo.showInfo("Zapisano bazę danych.");
			return true;
		} else if (id == R.id.action_reload) {
			treeManager.reset();
			treeManager.loadRootTree();
			updateItemsList();
			userInfo.showInfo("Wczytano bazę danych.");
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
		userInfo.showInfo("Anulowano edycję elementu.");
	}
	
	private void removeItem(final int position) {
		
		final TreeItem removing = treeManager.getCurrentItem().getChild(position);
		
		treeManager.getCurrentItem().remove(position);
		updateItemsList();
		userInfo.showInfoCancellable("Usunięto element: " + removing.getContent(), new InfoBarClickAction() {
			@Override
			public void onClick() {
				restoreItem(removing, position);
			}
		});
	}
	
	private void restoreItem(TreeItem restored, int position) {
		treeManager.getCurrentItem().add(position, restored);
		appData.setState(AppState.ITEMS_LIST);
		gui.showItemsList(treeManager.getCurrentItem());
		gui.scrollToItem(position);
		userInfo.showInfo("Przywrócono usunięty element.");
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
			userInfo.showInfo("Usunięto zaznaczone elementy: " + selectedIds.size());
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
			treeManager.clearClipboard();
			for (Integer selectedItemId : treeManager.getSelectedItems()) {
				TreeItem selectedItem = treeManager.getCurrentItem().getChild(selectedItemId);
				treeManager.addToClipboard(selectedItem);
			}
			//jeśli zaznaczony jeden element - skopiowanie do schowka
			if (treeManager.getClipboardSize() == 1) {
				TreeItem item = treeManager.getClipboard().get(0);
				clipboardManager.copyToSystemClipboard(item.getContent());
				if (info) {
					userInfo.showInfo("Skopiowano element: " + item.getContent());
				}
			} else {
				if (info) {
					userInfo.showInfo("Skopiowano zaznaczone elementy: " + treeManager.getClipboardSize());
				}
			}
		}
	}
	
	private void cutSelectedItems() {
		if (treeManager.isSelectionMode() && treeManager.getSelectedItemsCount() > 0) {
			copySelectedItems(false);
			userInfo.showInfo("Wycięto zaznaczone elementy: " + treeManager.getSelectedItemsCount());
			removeSelectedItems(false);
		} else {
			userInfo.showInfo("Brak zaznaczonych elementów");
		}
	}
	
	private void pasteItems() {
		if (treeManager.isClipboardEmpty()) {
			String systemClipboard = clipboardManager.getSystemClipboard();
			if (systemClipboard != null) {
				//wklejanie 1 elementu z systemowego schowka
				treeManager.getCurrentItem().add(systemClipboard);
				userInfo.showInfo("Wklejono element: " + systemClipboard);
				updateItemsList();
				gui.scrollToItem(-1);
			} else {
				userInfo.showInfo("Schowek jest pusty.");
			}
		} else {
			for (TreeItem clipboardItem : treeManager.getClipboard()) {
				clipboardItem.setParent(treeManager.getCurrentItem());
				treeManager.addToCurrent(clipboardItem);
			}
			userInfo.showInfo("Wklejono elementy: " + treeManager.getClipboardSize());
			treeManager.recopyClipboard();
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
				userInfo.showInfo("Skopiowano sumę do schowka: " + clipboardStr);
				
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
			userInfo.showInfo("Pusty element został usunięty.");
		} else {
			treeManager.getCurrentItem().add(treeManager.getNewItemPosition(), content);
			userInfo.showInfo("Zapisano nowy element.");
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
			treeManager.getCurrentItem().remove(editedItem);
			userInfo.showInfo("Pusty element został usunięty.");
		} else {
			editedItem.setContent(content);
			userInfo.showInfo("Zapisano element.");
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
				treeManager.setEditItem();
				appData.setState(AppState.ITEMS_LIST);
				gui.showItemsList(treeManager.getCurrentItem());
				restoreScrollPosition(treeManager.getCurrentItem());
				userInfo.showInfo("Pusty element został usunięty.");
			} else {
				editedItemIndex = treeManager.getNewItemPosition();
				treeManager.getCurrentItem().add(treeManager.getNewItemPosition(), content);
				userInfo.showInfo("Zapisano nowy element.");
			}
		} else { //edycja
			if (content.isEmpty()) {
				treeManager.getCurrentItem().remove(editedItem);
				treeManager.setEditItem();
				appData.setState(AppState.ITEMS_LIST);
				gui.showItemsList(treeManager.getCurrentItem());
				restoreScrollPosition(treeManager.getCurrentItem());
				userInfo.showInfo("Pusty element został usunięty.");
			} else {
				editedItemIndex = editedItem.getIndexInParent();
				editedItem.setContent(content);
				userInfo.showInfo("Zapisano element.");
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
				treeManager.setEditItem();
				appData.setState(AppState.ITEMS_LIST);
				gui.showItemsList(treeManager.getCurrentItem());
				restoreScrollPosition(treeManager.getCurrentItem());
				userInfo.showInfo("Pusty element został usunięty.");
				return;
			} else {
				treeManager.getCurrentItem().add(treeManager.getNewItemPosition(), content);
				newItemIndex++;
				userInfo.showInfo("Zapisano nowy element.");
			}
		} else { //edycja
			newItemIndex = editedItem.getIndexInParent();
			if (content.isEmpty()) {
				treeManager.getCurrentItem().remove(editedItem);
				treeManager.setEditItem();
				appData.setState(AppState.ITEMS_LIST);
				gui.showItemsList(treeManager.getCurrentItem());
				restoreScrollPosition(treeManager.getCurrentItem());
				userInfo.showInfo("Pusty element został usunięty.");
				return;
			} else {
				editedItem.setContent(content);
				newItemIndex++;
				userInfo.showInfo("Zapisano element.");
			}
		}
		//dodanie nowego elementu
		newItem(newItemIndex);
	}
	
	public void itemRemoveClicked(int position) {// removing locked before going into first element
		if (lock.isLocked()) {
			Logs.warn("Database is locked");
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
		lock.setLocked(false);
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
		if (treeManager.isSelectionMode()) {
			treeManager.toggleItemSelected(position);
			updateItemsList();
		} else {
			if (item.isEmpty()) {
				itemEditClicked(item);
			} else {
				
				// blokada wejścia wgłąb na pierwszym poziomie poprzez kliknięcie
				if (lock.isLocked()) {
					Logs.warn("Database is locked");
				} else {
					itemGoIntoClicked(position);
				}
				
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
	
	public void toolbarBackClicked() {backClicked();}
	
}
