package igrek.todotree.intent;


import javax.inject.Inject;

import igrek.todotree.app.AppData;
import igrek.todotree.app.AppState;
import igrek.todotree.dagger.DaggerIoc;
import igrek.todotree.domain.treeitem.AbstractTreeItem;
import igrek.todotree.service.tree.TreeManager;
import igrek.todotree.service.tree.TreeScrollCache;
import igrek.todotree.service.tree.TreeSelectionManager;
import igrek.todotree.ui.GUI;

public class GUICommand {
	
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
	
	public GUICommand() {
		DaggerIoc.factoryComponent.inject(this);
	}
	
	public void updateItemsList() {
		appData.setState(AppState.ITEMS_LIST);
		gui.updateItemsList(treeManager.getCurrentItem(), null, selectionManager.getSelectedItems());
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
