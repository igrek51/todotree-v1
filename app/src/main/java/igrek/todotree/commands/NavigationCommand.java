package igrek.todotree.commands;


import javax.inject.Inject;

import igrek.todotree.R;
import igrek.todotree.app.App;
import igrek.todotree.app.AppData;
import igrek.todotree.app.AppState;
import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.services.tree.TreeManager;
import igrek.todotree.services.tree.TreeSelectionManager;
import igrek.todotree.ui.GUI;

public class NavigationCommand {
	
	@Inject
	TreeManager treeManager;
	
	@Inject
	GUI gui;
	
	@Inject
	App app;
	
	@Inject
	AppData appData;
	
	@Inject
	TreeSelectionManager selectionManager;
	
	public NavigationCommand() {
		DaggerIOC.getAppComponent().inject(this);
	}
	
	public boolean optionsSelect(int id) {
		switch (id) {
			case R.id.action_minimize:
				app.minimize();
				return true;
			case R.id.action_exit_without_saving:
				new ExitCommand().exitApp();
				return true;
			case R.id.action_save_exit:
				new ExitCommand().optionSaveAndExit();
				return true;
			case R.id.action_save:
				new PersistenceCommand().optionSave();
				return true;
			case R.id.action_reload:
				new PersistenceCommand().optionReload();
				return true;
			case R.id.action_restore_backup:
				new PersistenceCommand().optionRestoreBackup();
				return true;
			case R.id.action_select_all:
				new ItemSelectionCommand().toggleSelectAll();
				return false;
			case R.id.action_cut:
				new ClipboardCommand().cutSelectedItems();
				return false;
			case R.id.action_copy:
				new ClipboardCommand().copySelectedItems();
				return false;
			case R.id.action_sum_selected:
				new ItemSelectionCommand().sumItems();
				return false;
		}
		return false;
	}
	
	public boolean backClicked() {
		if (appData.isState(AppState.ITEMS_LIST)) {
			if (selectionManager.isAnythingSelected()) {
				selectionManager.cancelSelectionMode();
				new GUICommand().updateItemsList();
			} else {
				new TreeCommand().goUp();
			}
		} else if (appData.isState(AppState.EDIT_ITEM_CONTENT)) {
			if (gui.editItemBackClicked())
				return true;
			new ItemEditorCommand().cancelEditedItem();
		}
		return true;
	}
	
}
