package igrek.todotree.actions;


import java.util.List;

import javax.inject.Inject;

import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.exceptions.NoSuperItemException;
import igrek.todotree.logger.Logs;
import igrek.todotree.model.treeitem.AbstractTreeItem;
import igrek.todotree.model.treeitem.LinkTreeItem;
import igrek.todotree.model.treeitem.TextTreeItem;
import igrek.todotree.services.history.ChangesHistory;
import igrek.todotree.services.lock.DatabaseLock;
import igrek.todotree.services.resources.UserInfoService;
import igrek.todotree.services.tree.TreeManager;
import igrek.todotree.services.tree.TreeMover;
import igrek.todotree.services.tree.TreeScrollCache;
import igrek.todotree.services.tree.TreeSelectionManager;
import igrek.todotree.ui.GUI;

public class TreeController {
	
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
	Logs logger;
	
	public TreeController() {
		DaggerIOC.getAppComponent().inject(this);
	}
	
	public void goUp() {
		try {
			AbstractTreeItem current = treeManager.getCurrentItem();
			AbstractTreeItem parent = current.getParent();
			treeManager.goUp();
			new GUIController().updateItemsList();
			new GUIController().restoreScrollPosition(parent);
		} catch (NoSuperItemException e) {
			new ExitController().saveAndExitRequested();
		}
	}
	
	public void itemGoIntoClicked(int position, AbstractTreeItem item) {
		if (lock.isLocked()) {
			lock.setLocked(false);
			logger.debug("Database unlocked.");
		}
		if (item instanceof LinkTreeItem) {
			goToLinkTarget((LinkTreeItem) item);
		} else {
			selectionManager.cancelSelectionMode();
			goInto(position);
			new GUIController().updateItemsList();
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
		new GUIController().updateItemsList();
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
			new GUIController().updateItemsList();
			gui.scrollToItem(position);
		} else {
			selectionManager.setItemSelected(position, true);
			new GUIController().updateItemsList();
		}
	}
	
	public void itemClicked(int position, AbstractTreeItem item) {
		lock.assertUnlocked();
		if (selectionManager.isAnythingSelected()) {
			selectionManager.toggleItemSelected(position);
			new GUIController().updateItemsList();
		} else {
			if (item instanceof TextTreeItem) {
				if (item.isEmpty()) {
					new ItemEditorController().itemEditClicked(item);
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
