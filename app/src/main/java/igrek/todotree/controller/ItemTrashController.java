package igrek.todotree.controller;


import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.datatree.TreeManager;
import igrek.todotree.datatree.TreeSelectionManager;
import igrek.todotree.datatree.item.TreeItem;
import igrek.todotree.gui.GUI;
import igrek.todotree.services.lock.DatabaseLock;
import igrek.todotree.services.resources.InfoBarClickAction;
import igrek.todotree.services.resources.UserInfoService;

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
	
	public void removeItems(List<Integer> selectedIds, boolean info) {
		//posortowanie malejąco (żeby przy usuwaniu nie nadpisać indeksów)
		Collections.sort(selectedIds, new Comparator<Integer>() {
			@Override
			public int compare(Integer lhs, Integer rhs) {
				return rhs.compareTo(lhs);
			}
		});
		for (Integer id : selectedIds) {
			treeManager.removeFromCurrent(id);
		}
		if (info) {
			userInfo.showInfo("Items removed: " + selectedIds.size());
		}
		selectionManager.cancelSelectionMode();
		new GUIController().updateItemsList();
	}
}
