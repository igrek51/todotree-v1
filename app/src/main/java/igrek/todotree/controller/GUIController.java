package igrek.todotree.controller;


import javax.inject.Inject;

import igrek.todotree.app.AppData;
import igrek.todotree.app.AppState;
import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.datatree.TreeManager;
import igrek.todotree.datatree.item.TreeItem;
import igrek.todotree.gui.GUI;

public class GUIController {
	
	@Inject
	TreeManager treeManager;
	
	@Inject
	GUI gui;
	
	@Inject
	AppData appData;
	
	public GUIController() {
		DaggerIOC.getAppComponent().inject(this);
	}
	
	public void updateItemsList() {
		appData.setState(AppState.ITEMS_LIST);
		gui.updateItemsList(treeManager.getCurrentItem(), treeManager.selectionManager()
				.getSelectedItems());
	}
	
	public void showItemsList() {
		appData.setState(AppState.ITEMS_LIST);
		gui.showItemsList(treeManager.getCurrentItem());
	}
	
	public void restoreScrollPosition(TreeItem parent) {
		Integer savedScrollPos = treeManager.scrollCache().restoreScrollPosition(parent);
		if (savedScrollPos != null) {
			gui.scrollToPosition(savedScrollPos);
		}
	}
}
