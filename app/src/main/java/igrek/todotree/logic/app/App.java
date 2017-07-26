package igrek.todotree.logic.app;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import igrek.todotree.R;
import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.gui.GUI;
import igrek.todotree.logger.Logs;
import igrek.todotree.logic.backup.BackupManager;
import igrek.todotree.logic.controller.AppController;
import igrek.todotree.logic.controller.dispatcher.IEvent;
import igrek.todotree.logic.controller.dispatcher.IEventObserver;
import igrek.todotree.logic.datatree.TreeItem;
import igrek.todotree.logic.datatree.TreeManager;
import igrek.todotree.logic.events.AddItemClickedEvent;
import igrek.todotree.logic.events.AddItemClickedPosEvent;
import igrek.todotree.logic.events.CancelEditedItemEvent;
import igrek.todotree.logic.events.ExitAppEvent;
import igrek.todotree.logic.events.ItemClickedEvent;
import igrek.todotree.logic.events.ItemEditClickedEvent;
import igrek.todotree.logic.events.ItemGoIntoClickedEvent;
import igrek.todotree.logic.events.ItemLongClickEvent;
import igrek.todotree.logic.events.ItemMovedEvent;
import igrek.todotree.logic.events.ItemRemoveClickedEvent;
import igrek.todotree.logic.events.SaveAndAddItemEvent;
import igrek.todotree.logic.events.SaveAndGoIntoItemEvent;
import igrek.todotree.logic.events.SavedEditedItemEvent;
import igrek.todotree.logic.events.SavedNewItemEvent;
import igrek.todotree.logic.events.SelectedItemClickedEvent;
import igrek.todotree.logic.events.ToolbarBackClickedEvent;
import igrek.todotree.logic.exceptions.NoSuperItemException;
import igrek.todotree.preferences.Preferences;
import igrek.todotree.resources.InfoBarClickAction;
import igrek.todotree.resources.UserInfoService;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

//TODO brak zapisu bazy jeśli nie było zmian

//TODO: funkcja cofania zmian - zapisywanie modyfikacji, dodawania, usuwania elementów, przesuwania

//TODO SERWISY: do blokowania bazy, do pobierania treści komunikatów

public class App extends BaseApp implements IEventObserver {
	
	@Inject
	TreeManager treeManager;
	@Inject
	BackupManager backupManager;
	@Inject
	UserInfoService userInfo;
	@Inject
	Preferences preferences;
	@Inject
	GUI gui;
	
	private AppState state;
	
	public App(AppCompatActivity activity) {
		super(activity);
		// Dagger init
		DaggerIOC.init(activity);
		DaggerIOC.getAppComponent().inject(this);
	}
	
	@Override
	public void init() {
		super.init();
		
		registerEventObservers();
		
		treeManager.loadRootTree();
		
		gui = new GUI(activity);
		gui.showItemsList(treeManager.getCurrentItem());
		state = AppState.ITEMS_LIST;
		
		Logs.info("Application started.");
	}
	
	private void registerEventObservers() {
		AppController.registerEventObserver(AddItemClickedEvent.class, this);
		AppController.registerEventObserver(AddItemClickedPosEvent.class, this);
		AppController.registerEventObserver(CancelEditedItemEvent.class, this);
		AppController.registerEventObserver(ItemClickedEvent.class, this);
		AppController.registerEventObserver(ItemEditClickedEvent.class, this);
		AppController.registerEventObserver(ItemGoIntoClickedEvent.class, this);
		AppController.registerEventObserver(ItemLongClickEvent.class, this);
		AppController.registerEventObserver(ItemRemoveClickedEvent.class, this);
		AppController.registerEventObserver(SaveAndAddItemEvent.class, this);
		AppController.registerEventObserver(SaveAndGoIntoItemEvent.class, this);
		AppController.registerEventObserver(SavedEditedItemEvent.class, this);
		AppController.registerEventObserver(SavedNewItemEvent.class, this);
		AppController.registerEventObserver(SelectedItemClickedEvent.class, this);
		AppController.registerEventObserver(ToolbarBackClickedEvent.class, this);
		AppController.registerEventObserver(ItemMovedEvent.class, this);
		AppController.registerEventObserver(ExitAppEvent.class, this);
	}
	
	@Override
	public void quit() {
		preferences.saveAll();
		super.quit();
	}
	
	@Override
	public boolean optionsSelect(int id) {
		if (id == R.id.action_minimize) {
			minimize();
			return true;
		} else if (id == R.id.action_exit_without_saving) {
			exitApp(false);
			return true;
		} else if (id == R.id.action_save_exit) {
			if (state == AppState.EDIT_ITEM_CONTENT) {
				gui.requestSaveEditedItem();
			}
			exitApp(true);
			return true;
		} else if (id == R.id.action_save) {
			saveDatabase();
			showInfo("Zapisano bazę danych.");
			return true;
		} else if (id == R.id.action_reload) {
			treeManager.reset();
			treeManager.loadRootTree();
			updateItemsList();
			showInfo("Wczytano bazę danych.");
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
	
	@Override
	public boolean onKeyBack() {
		backClicked();
		return true;
	}
	
	
	private void showInfo(String info) {
		userInfo.showActionInfo(info, gui.getMainContent(), "OK", null, null);
	}
	
	private void showInfoCancellable(String info, InfoBarClickAction cancelCallback) {
		userInfo.showActionInfo(info, gui.getMainContent(), "Cofnij", cancelCallback, ContextCompat.getColor(activity, R.color.colorPrimary));
	}
	
	
	@Override
	public void menuInit(Menu menu) {
		super.menuInit(menu);
		
		//TODO: zmiana widoczności opcji menu przy zaznaczaniu wielu elementów i kopiowaniu (niepusty schowek, niepuste zaznaczenie)
		//TODO: zmiana widoczności opcji menu przy edycji elementu
		
		//setMenuItemVisible(R.id.action_copy, false);
		//item.setTitle(title);
		//item.setIcon(iconRes); //int iconRes
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
		state = AppState.EDIT_ITEM_CONTENT;
	}
	
	private void editItem(TreeItem item, TreeItem parent) {
		treeManager.storeScrollPosition(treeManager.getCurrentItem(), gui.getCurrentScrollPos());
		treeManager.setEditItem(item);
		gui.showEditItemPanel(item, parent);
		state = AppState.EDIT_ITEM_CONTENT;
	}
	
	private void discardEditingItem() {
		treeManager.setEditItem(null);
		state = AppState.ITEMS_LIST;
		gui.showItemsList(treeManager.getCurrentItem());
		restoreScrollPosition(treeManager.getCurrentItem());
		showInfo("Anulowano edycję elementu.");
	}
	
	private void removeItem(final int position) {
		
		final TreeItem removing = treeManager.getCurrentItem().getChild(position);
		
		treeManager.getCurrentItem().remove(position);
		updateItemsList();
		showInfoCancellable("Usunięto element: " + removing.getContent(), new InfoBarClickAction() {
			@Override
			public void onClick() {
				restoreItem(removing, position);
			}
		});
	}
	
	private void restoreItem(TreeItem restored, int position) {
		treeManager.getCurrentItem().add(position, restored);
		state = AppState.ITEMS_LIST;
		gui.showItemsList(treeManager.getCurrentItem());
		gui.scrollToItem(position);
		showInfo("Przywrócono usunięty element.");
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
			showInfo("Usunięto zaznaczone elementy: " + selectedIds.size());
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
	
	private void backClicked() {
		if (state == AppState.ITEMS_LIST) {
			if (treeManager.isSelectionMode()) {
				treeManager.cancelSelectionMode();
				updateItemsList();
			} else {
				goUp();
			}
		} else if (state == AppState.EDIT_ITEM_CONTENT) {
			if (gui.editItemBackClicked())
				return;
			discardEditingItem();
		}
	}
	
	private void exitApp(boolean withSaving) {
		//TODO Najpierw wyświetlić exit screen, potem zapisywać bazę
		if (withSaving) {
			saveDatabase();
		}
		gui.showExitScreen();
	}
	
	private void updateItemsList() {
		gui.updateItemsList(treeManager.getCurrentItem(), treeManager.getSelectedItems());
		state = AppState.ITEMS_LIST;
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
				copyToSystemClipboard(item.getContent());
				if (info) {
					showInfo("Skopiowano element: " + item.getContent());
				}
			} else {
				if (info) {
					showInfo("Skopiowano zaznaczone elementy: " + treeManager.getClipboardSize());
				}
			}
		}
	}
	
	private void cutSelectedItems() {
		if (treeManager.isSelectionMode() && treeManager.getSelectedItemsCount() > 0) {
			copySelectedItems(false);
			showInfo("Wycięto zaznaczone elementy: " + treeManager.getSelectedItemsCount());
			removeSelectedItems(false);
		} else {
			showInfo("Brak zaznaczonych elementów");
		}
	}
	
	private void pasteItems() {
		if (treeManager.isClipboardEmpty()) {
			String systemClipboard = getSystemClipboard();
			if (systemClipboard != null) {
				//wklejanie 1 elementu z systemowego schowka
				treeManager.getCurrentItem().add(systemClipboard);
				showInfo("Wklejono element: " + systemClipboard);
				updateItemsList();
				gui.scrollToItem(-1);
			} else {
				showInfo("Schowek jest pusty.");
			}
		} else {
			for (TreeItem clipboardItem : treeManager.getClipboard()) {
				clipboardItem.setParent(treeManager.getCurrentItem());
				treeManager.addToCurrent(clipboardItem);
			}
			showInfo("Wklejono elementy: " + treeManager.getClipboardSize());
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
	
	private void goToRoot() {
		if (state == AppState.ITEMS_LIST) {
			if (treeManager.isSelectionMode()) {
				treeManager.cancelSelectionMode();
			}
			treeManager.goToRoot();
			updateItemsList();
		} else if (state == AppState.EDIT_ITEM_CONTENT) {
			gui.hideSoftKeyboard();
			discardEditingItem();
			treeManager.goToRoot();
			gui.showItemsList(treeManager.getCurrentItem());
			state = AppState.ITEMS_LIST;
		}
	}
	
	
	private void sumSelected() {
		if (treeManager.isSelectionMode()) {
			try {
				BigDecimal sum = treeManager.sumSelected();
				
				String clipboardStr = sum.toPlainString();
				clipboardStr = clipboardStr.replace('.', ',');
				
				copyToSystemClipboard(clipboardStr);
				showInfo("Skopiowano sumę do schowka: " + clipboardStr);
				
			} catch (NumberFormatException e) {
				showInfo(e.getMessage());
			}
		}
	}
	
	private void saveDatabase() {
		treeManager.saveRootTree();
		backupManager.saveBackupFile();
	}
	
	private void copyToSystemClipboard(String text) {
		ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Activity.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("Copied Text", text);
		clipboard.setPrimaryClip(clip);
	}
	
	private String getSystemClipboard() {
		ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Activity.CLIPBOARD_SERVICE);
		if (clipboard.hasPrimaryClip() && clipboard.getPrimaryClipDescription()
				.hasMimeType(MIMETYPE_TEXT_PLAIN)) {
			ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
			if (item == null)
				return null;
			if (item.getText() == null)
				return null;
			return item.getText().toString();
		}
		return null;
	}
	
	@Override
	public void onEvent(IEvent event) {
		//TODO przenieść eventy do odpowiedzialnych klas
		if (event instanceof ToolbarBackClickedEvent) {
			
			backClicked();
			
		} else if (event instanceof AddItemClickedEvent) {
			
			//TODO zamienić na jeden event
			AppController.sendEvent(new AddItemClickedPosEvent(-1));
			
		} else if (event instanceof AddItemClickedPosEvent) {
			
			treeManager.cancelSelectionMode();
			newItem(((AddItemClickedPosEvent) event).getPosition());
			
		} else if (event instanceof CancelEditedItemEvent) {
			
			gui.hideSoftKeyboard();
			discardEditingItem();
			
		} else if (event instanceof ItemClickedEvent) {
			
			int position = ((ItemClickedEvent) event).getPosition();
			TreeItem item = ((ItemClickedEvent) event).getTreeItem();
			if (treeManager.isSelectionMode()) {
				treeManager.toggleItemSelected(position);
				updateItemsList();
			} else {
				if (item.isEmpty()) {
					AppController.sendEvent(new ItemEditClickedEvent(position, item));
				} else {
					
					// blokada wejścia wgłąb na pierwszym poziomie poprzez kliknięcie
					if (treeManager.firstGoInto || treeManager.getCurrentItem()
							.getParent() != null) {
						AppController.sendEvent(new ItemGoIntoClickedEvent(position, item));
					}
					
				}
			}
			
		} else if (event instanceof ItemEditClickedEvent) {
			
			treeManager.cancelSelectionMode();
			editItem(((ItemEditClickedEvent) event).getTreeItem(), treeManager.getCurrentItem());
			
		} else if (event instanceof ItemGoIntoClickedEvent) {
			
			treeManager.firstGoInto = true;
			treeManager.cancelSelectionMode();
			treeManager.goInto(((ItemGoIntoClickedEvent) event).getPosition(), gui.getCurrentScrollPos());
			updateItemsList();
			gui.scrollToItem(0);
			
		} else if (event instanceof ItemLongClickEvent) {
			
			int position = ((ItemLongClickEvent) event).getPosition();
			if (!treeManager.isSelectionMode()) {
				treeManager.startSelectionMode();
				treeManager.setItemSelected(position, true);
				updateItemsList();
				gui.scrollToItem(position);
			} else {
				treeManager.setItemSelected(position, true);
				updateItemsList();
			}
			
		} else if (event instanceof ItemRemoveClickedEvent) {
			
			// removing locked before going into first element
			if (treeManager.firstGoInto || treeManager.getCurrentItem().getParent() != null) {
				if (treeManager.isSelectionMode()) {
					removeSelectedItems(true);
				} else {
					removeItem(((ItemRemoveClickedEvent) event).getPosition());
				}
			} else {
				Logs.warn("Database is locked");
			}
			
		} else if (event instanceof SaveAndAddItemEvent) {
			
			String content = ((SaveAndAddItemEvent) event).getContent();
			TreeItem editedItem = ((SaveAndAddItemEvent) event).getEditedItem();
			//zapis
			content = treeManager.trimContent(content);
			int newItemIndex;
			if (editedItem == null) { //nowy element
				newItemIndex = treeManager.getNewItemPosition();
				if (content.isEmpty()) {
					treeManager.setEditItem(null);
					state = AppState.ITEMS_LIST;
					gui.showItemsList(treeManager.getCurrentItem());
					restoreScrollPosition(treeManager.getCurrentItem());
					showInfo("Pusty element został usunięty.");
					return;
				} else {
					treeManager.getCurrentItem().add(treeManager.getNewItemPosition(), content);
					newItemIndex++;
					showInfo("Zapisano nowy element.");
				}
			} else { //edycja
				newItemIndex = editedItem.getIndexInParent();
				if (content.isEmpty()) {
					treeManager.getCurrentItem().remove(editedItem);
					treeManager.setEditItem(null);
					state = AppState.ITEMS_LIST;
					gui.showItemsList(treeManager.getCurrentItem());
					restoreScrollPosition(treeManager.getCurrentItem());
					showInfo("Pusty element został usunięty.");
					return;
				} else {
					editedItem.setContent(content);
					newItemIndex++;
					showInfo("Zapisano element.");
				}
			}
			//dodanie nowego elementu
			newItem(newItemIndex);
		} else if (event instanceof SaveAndGoIntoItemEvent) {
			
			String content = ((SaveAndGoIntoItemEvent) event).getContent();
			TreeItem editedItem = ((SaveAndGoIntoItemEvent) event).getEditedItem();
			//zapis
			content = treeManager.trimContent(content);
			Integer editedItemIndex = null;
			if (editedItem == null) { //nowy element
				if (content.isEmpty()) {
					treeManager.setEditItem(null);
					state = AppState.ITEMS_LIST;
					gui.showItemsList(treeManager.getCurrentItem());
					restoreScrollPosition(treeManager.getCurrentItem());
					showInfo("Pusty element został usunięty.");
				} else {
					editedItemIndex = treeManager.getNewItemPosition();
					treeManager.getCurrentItem().add(treeManager.getNewItemPosition(), content);
					showInfo("Zapisano nowy element.");
				}
			} else { //edycja
				if (content.isEmpty()) {
					treeManager.getCurrentItem().remove(editedItem);
					treeManager.setEditItem(null);
					state = AppState.ITEMS_LIST;
					gui.showItemsList(treeManager.getCurrentItem());
					restoreScrollPosition(treeManager.getCurrentItem());
					showInfo("Pusty element został usunięty.");
				} else {
					editedItemIndex = editedItem.getIndexInParent();
					editedItem.setContent(content);
					showInfo("Zapisano element.");
				}
			}
			if (editedItemIndex != null) {
				//wejście wewnątrz
				treeManager.goInto(editedItemIndex, gui.getCurrentScrollPos());
				//dodawanie nowego elementu na końcu
				newItem(-1);
			}
			
		} else if (event instanceof SavedEditedItemEvent) {
			
			TreeItem editedItem = ((SavedEditedItemEvent) event).getEditedItem();
			String content = ((SavedEditedItemEvent) event).getContent();
			
			content = treeManager.trimContent(content);
			if (content.isEmpty()) {
				treeManager.getCurrentItem().remove(editedItem);
				showInfo("Pusty element został usunięty.");
			} else {
				editedItem.setContent(content);
				showInfo("Zapisano element.");
			}
			treeManager.setEditItem(null);
			state = AppState.ITEMS_LIST;
			gui.showItemsList(treeManager.getCurrentItem());
			restoreScrollPosition(treeManager.getCurrentItem());
			
		} else if (event instanceof SavedNewItemEvent) {
			
			String content = ((SavedNewItemEvent) event).getContent();
			content = treeManager.trimContent(content);
			if (content.isEmpty()) {
				showInfo("Pusty element został usunięty.");
			} else {
				treeManager.getCurrentItem().add(treeManager.getNewItemPosition(), content);
				showInfo("Zapisano nowy element.");
			}
			state = AppState.ITEMS_LIST;
			gui.showItemsList(treeManager.getCurrentItem());
			if (treeManager.getNewItemPosition() == treeManager.getCurrentItem().size() - 1) {
				gui.scrollToBottom();
			} else {
				restoreScrollPosition(treeManager.getCurrentItem());
			}
			treeManager.setNewItemPosition(null);
			
		} else if (event instanceof SelectedItemClickedEvent) {
			
			int position = ((SelectedItemClickedEvent) event).getPosition();
			boolean checked = ((SelectedItemClickedEvent) event).isChecked();
			treeManager.setItemSelected(position, checked);
			updateItemsList();
			
		} else if (event instanceof ItemMovedEvent) {
			
			int position = ((ItemMovedEvent) event).getPosition();
			int step = ((ItemMovedEvent) event).getStep();
			treeManager.move(treeManager.getCurrentItem(), position, step);
			
		} else if (event instanceof ExitAppEvent) {
			quit();
		}
	}
}
