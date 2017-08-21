package igrek.todotree.actions;


import javax.inject.Inject;

import igrek.todotree.app.AppData;
import igrek.todotree.app.AppState;
import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.model.treeitem.AbstractTreeItem;
import igrek.todotree.services.tree.TreeManager;
import igrek.todotree.services.tree.TreeScrollCache;
import igrek.todotree.services.tree.TreeSelectionManager;
import igrek.todotree.ui.GUI;

public class GUIController {
	
	@Inject
	TreeManager treeManager;
	
	@Inject
	GUI gui;
	
	@Inject
	AppData appData;
	
	@Inject
	TreeScrollCache scrollCache;
	
	@Inject
	TreeSelectionManager selectionManager;
	
	public GUIController() {
		DaggerIOC.getAppComponent().inject(this);
	}
	
	public void updateItemsList() {
		appData.setState(AppState.ITEMS_LIST);
		gui.updateItemsList(treeManager.getCurrentItem(), selectionManager.getSelectedItems());
	}
	
	public void showItemsList() {
		appData.setState(AppState.ITEMS_LIST);
		gui.showItemsList(treeManager.getCurrentItem());
	}
	
	public void restoreScrollPosition(AbstractTreeItem parent) {
		Integer savedScrollPos = scrollCache.restoreScrollPosition(parent);
		if (savedScrollPos != null) {
			gui.scrollToPosition(savedScrollPos);
		}
	}
	
	public void guiInit() {
		gui.lazyInit();
	}
	
	public void numKeyboardHyphenTyped() {
		gui.quickInsertRange();
	}
}
