package igrek.todotree.commands;


import java.util.Iterator;
import java.util.TreeSet;

import javax.inject.Inject;

import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.domain.treeitem.AbstractTreeItem;
import igrek.todotree.domain.treeitem.LinkTreeItem;
import igrek.todotree.service.access.DatabaseLock;
import igrek.todotree.service.resources.InfoBarClickAction;
import igrek.todotree.service.resources.UserInfoService;
import igrek.todotree.service.tree.TreeManager;
import igrek.todotree.service.tree.TreeSelectionManager;
import igrek.todotree.ui.GUI;

public class ItemTrashCommand {
	
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
	
	public ItemTrashCommand() {
		DaggerIOC.getFactoryComponent().inject(this);
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
		final AbstractTreeItem removing = treeManager.getCurrentItem().getChild(position);
		
		treeManager.removeFromCurrent(position);
		new GUICommand().updateItemsList();
		userInfo.showInfoCancellable("Item removed: " + removing.getDisplayName(), new InfoBarClickAction() {
			@Override
			public void onClick() {
				restoreRemovedItem(removing, position);
			}
		});
	}
	
	private void restoreRemovedItem(AbstractTreeItem restored, int position) {
		treeManager.addToCurrent(position, restored);
		new GUICommand().showItemsList();
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
		new GUICommand().updateItemsList();
	}
	
	void removeLinkAndTarget(int linkPosition, LinkTreeItem linkItem) {
		lock.assertUnlocked();
		
		// remove link
		treeManager.removeFromCurrent(linkPosition);
		
		// remove target
		AbstractTreeItem target = linkItem.getTarget();
		final Runnable restoreTargetAction;
		if (target != null) {
			AbstractTreeItem parent = target.getParent();
			int removedIndex = target.removeItself();
			restoreTargetAction = () -> {
				if (removedIndex >= 0) {
					parent.add(removedIndex, target);
				}
			};
		} else {
			restoreTargetAction = () -> {
			};
		}
		
		new GUICommand().updateItemsList();
		userInfo.showInfoCancellable("Link & item removed: " + linkItem.getDisplayName(), () -> {
			// restore target
			restoreTargetAction.run();
			
			// restore link
			treeManager.addToCurrent(linkPosition, linkItem);
			
			new GUICommand().showItemsList();
			gui.scrollToItem(linkPosition);
			userInfo.showInfo("Removed items restored.");
		});
	}
}
