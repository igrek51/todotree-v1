package igrek.todotree.controller;


import javax.inject.Inject;

import igrek.todotree.R;
import igrek.todotree.app.App;
import igrek.todotree.app.AppData;
import igrek.todotree.app.AppState;
import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.datatree.TreeManager;
import igrek.todotree.datatree.TreeSelectionManager;
import igrek.todotree.ui.GUI;

public class MainController {
	
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
	
	public MainController() {
		DaggerIOC.getAppComponent().inject(this);
	}
	
	public boolean optionsSelect(int id) {
		switch (id) {
			case R.id.action_minimize:
				app.minimize();
				return true;
			case R.id.action_exit_without_saving:
				new ExitController().exitApp();
				return true;
			case R.id.action_save_exit:
				new ExitController().optionSaveAndExit();
				return true;
			case R.id.action_save:
				new PersistenceController().optionSave();
				return true;
			case R.id.action_reload:
				new PersistenceController().optionReload();
				return true;
			case R.id.action_select_all:
				new ItemSelectionController().toggleSelectAll();
				return false;
			case R.id.action_sum_selected:
				new ItemSelectionController().sumItems();
				return false;
		}
		return false;
	}
	
	public void backClicked() {
		if (appData.isState(AppState.ITEMS_LIST)) {
			if (selectionManager.isAnythingSelected()) {
				selectionManager.cancelSelectionMode();
				new GUIController().updateItemsList();
			} else {
				new TreeController().goUp();
			}
		} else if (appData.isState(AppState.EDIT_ITEM_CONTENT)) {
			if (gui.editItemBackClicked())
				return;
			new ItemEditorController().cancelEditedItem();
		}
	}
	
}
