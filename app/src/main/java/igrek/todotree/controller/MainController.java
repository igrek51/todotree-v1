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
	
	public void restoreScrollPosition(TreeItem parent) {
		Integer savedScrollPos = treeManager.scrollCache().restoreScrollPosition(parent);
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
			new ItemEditController().cancelEditedItem();
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
	
	
	public void showItemsList() {
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
				new ItemEditController().itemEditClicked(item);
			} else {
				itemGoIntoClicked(position);
			}
		}
	}
	
	public void addItemHereClicked(int position) {
		treeManager.selectionManager().cancelSelectionMode();
		new ItemEditController().newItem(position);
	}
	
	public void addItemClicked() {
		addItemHereClicked(-1);
	}
	
}
