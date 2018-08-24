package igrek.todotree.commands;


import java.util.List;

import javax.inject.Inject;

import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.domain.treeitem.AbstractTreeItem;
import igrek.todotree.domain.treeitem.LinkTreeItem;
import igrek.todotree.domain.treeitem.TextTreeItem;
import igrek.todotree.exceptions.NoSuperItemException;
import igrek.todotree.logger.Logger;
import igrek.todotree.service.access.DatabaseLock;
import igrek.todotree.service.history.ChangesHistory;
import igrek.todotree.service.history.LinkHistoryService;
import igrek.todotree.service.resources.UserInfoService;
import igrek.todotree.service.tree.TreeManager;
import igrek.todotree.service.tree.TreeMover;
import igrek.todotree.service.tree.TreeScrollCache;
import igrek.todotree.service.tree.TreeSelectionManager;
import igrek.todotree.ui.GUI;

public class TreeCommand {
	
	@Inject
	TreeManager treeManager;
	
	@Inject
	GUI gui;
	
	@Inject
	DatabaseLock lock;
	
	@Inject
	TreeScrollCache scrollCache;
	
	@Inject
	TreeSelectionManager selectionManager;
	
	@Inject
	TreeMover treeMover;
	
	@Inject
	ChangesHistory changesHistory;
	
	@Inject
	UserInfoService infoService;
	
	@Inject
	Logger logger;
	
	@Inject
	LinkHistoryService linkHistoryService;
	
	public TreeCommand() {
		DaggerIOC.getFactoryComponent().inject(this);
	}
	
	public void goBack() {
		try {
			AbstractTreeItem current = treeManager.getCurrentItem();
			AbstractTreeItem parent = current.getParent();
			// if item was reached from link - go back to link parent
			if (linkHistoryService.hasLink(current)) {
				LinkTreeItem linkFromTarget = linkHistoryService.getLinkFromTarget(current);
				linkHistoryService.resetTarget(current);
				parent = linkFromTarget.getParent();
				treeManager.goTo(parent);
			} else {
				treeManager.goUp();
				linkHistoryService.resetTarget(current); // reset link target - just in case
			}
			new GUICommand().updateItemsList();
			new GUICommand().restoreScrollPosition(parent);
		} catch (NoSuperItemException e) {
			new ExitCommand().saveAndExitRequested();
		}
	}
	
	public void goUp() {
		try {
			AbstractTreeItem current = treeManager.getCurrentItem();
			AbstractTreeItem parent = current.getParent();
			treeManager.goUp();
			linkHistoryService.resetTarget(current); // reset link target - just in case
			new GUICommand().updateItemsList();
			new GUICommand().restoreScrollPosition(parent);
		} catch (NoSuperItemException e) {
		}
	}
	
	public void itemGoIntoClicked(int position, AbstractTreeItem item) {
		lock.unlockIfLocked(item);
		if (item instanceof LinkTreeItem) {
			goToLinkTarget((LinkTreeItem) item);
		} else {
			selectionManager.cancelSelectionMode();
			goInto(position);
			new GUICommand().updateItemsList();
			gui.scrollToItem(0);
		}
	}
	
	public void goInto(int childIndex) {
		storeCurrentScroll();
		treeManager.goInto(childIndex);
	}
	
	public void navigateTo(AbstractTreeItem item) {
		storeCurrentScroll();
		treeManager.goTo(item);
		new GUICommand().updateItemsList();
		gui.scrollToItem(0);
	}
	
	private void storeCurrentScroll() {
		Integer scrollPos = gui.getCurrentScrollPos();
		if (scrollPos != null) {
			scrollCache.storeScrollPosition(treeManager.getCurrentItem(), scrollPos);
		}
	}
	
	public void itemLongClicked(int position) {
		if (!selectionManager.isAnythingSelected()) {
			selectionManager.startSelectionMode();
			selectionManager.setItemSelected(position, true);
			new GUICommand().updateItemsList();
			gui.scrollToItem(position);
		} else {
			selectionManager.setItemSelected(position, true);
			new GUICommand().updateItemsList();
		}
	}
	
	public void itemClicked(int position, AbstractTreeItem item) {
		lock.assertUnlocked();
		if (selectionManager.isAnythingSelected()) {
			selectionManager.toggleItemSelected(position);
			new GUICommand().updateItemsList();
		} else {
			if (item instanceof TextTreeItem) {
				if (item.isEmpty()) {
					new ItemEditorCommand().itemEditClicked(item);
				} else {
					itemGoIntoClicked(position, item);
				}
			} else if (item instanceof LinkTreeItem) {
				goToLinkTarget((LinkTreeItem) item);
			}
		}
	}
	
	private void goToLinkTarget(LinkTreeItem item) {
		// go into target
		LinkTreeItem link = item;
		AbstractTreeItem target = link.getTarget();
		if (target == null) {
			infoService.showInfo("Link is broken: " + link.getDisplayTargetPath());
		} else {
			linkHistoryService.storeTargetLink(target, link);
			navigateTo(target);
		}
	}
	
	public List<AbstractTreeItem> itemMoved(int position, int step) {
		treeMover.move(treeManager.getCurrentItem(), position, step);
		changesHistory.registerChange();
		return treeManager.getCurrentItem().getChildren();
	}
	
	public AbstractTreeItem findItemByPath(String[] paths) {
		AbstractTreeItem current = treeManager.getRootItem();
		for (String path : paths) {
			AbstractTreeItem found = current.findChildByName(path);
			if (found == null)
				return null;
			current = found;
		}
		return current;
	}
}
