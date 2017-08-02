package igrek.todotree.controller;


import java.util.Iterator;
import java.util.TreeSet;

import javax.inject.Inject;

import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.model.tree.TreeItem;
import igrek.todotree.services.lock.DatabaseLock;
import igrek.todotree.services.resources.InfoBarClickAction;
import igrek.todotree.services.resources.UserInfoService;
import igrek.todotree.services.tree.TreeManager;
import igrek.todotree.services.tree.TreeSelectionManager;
import igrek.todotree.ui.GUI;

public class ItemTrashController {
	
	@Inject
	TreeManager treeManager;
	
	@Inject
	GUI gui;
	
	@Inject
	UserInfoService userInfo;
	
	@Inject
	DatabaseLock lock;
	
	@Inject
	TreeSelectionManager selectionManager;
	
	public ItemTrashController() {
		DaggerIOC.getAppComponent().inject(this);
	}
	
	public void itemRemoveClicked(int position) {// removing locked before going into first element
		lock.assertUnlocked();
		if (selectionManager.isAnythingSelected()) {
			removeSelectedItems(true);
		} else {
			removeItem(position);
		}
	}
	
	private void removeItem(final int position) {
		final TreeItem removing = treeManager.getCurrentItem().getChild(position);
		
		treeManager.removeFromCurrent(position);
		new GUIController().updateItemsList();
		userInfo.showInfoCancellable("Item removed: " + removing.getContent(), new InfoBarClickAction() {
			@Override
			public void onClick() {
				restoreRemovedItem(removing, position);
			}
		});
	}
	
	private void restoreRemovedItem(TreeItem restored, int position) {
		treeManager.addToCurrent(position, restored);
		new GUIController().showItemsList();
		gui.scrollToItem(position);
		userInfo.showInfo("Removed item restored.");
	}
	
	public void removeSelectedItems(boolean info) {
		removeItems(selectionManager.getSelectedItems(), info);
	}
	
	public void removeItems(TreeSet<Integer> selectedIds, boolean info) {
		//descending order in order to not overwriting indices when removing
		Iterator<Integer> iterator = selectedIds.descendingIterator();
		while (iterator.hasNext()) {
			treeManager.removeFromCurrent(iterator.next());
		}
		if (info) {
			userInfo.showInfo("Items removed: " + selectedIds.size());
		}
		selectionManager.cancelSelectionMode();
		new GUIController().updateItemsList();
	}
}
