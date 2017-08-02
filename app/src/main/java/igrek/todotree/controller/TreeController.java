package igrek.todotree.controller;


import javax.inject.Inject;

import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.datatree.TreeManager;
import igrek.todotree.datatree.TreeScrollCache;
import igrek.todotree.datatree.item.TreeItem;
import igrek.todotree.exceptions.NoSuperItemException;
import igrek.todotree.gui.GUI;
import igrek.todotree.logger.Logs;
import igrek.todotree.services.lock.DatabaseLock;

public class TreeController {
	
	@Inject
	TreeManager treeManager;
	
	@Inject
	GUI gui;
	
	@Inject
	DatabaseLock lock;
	
	@Inject
	TreeScrollCache scrollCache;
	
	public TreeController() {
		DaggerIOC.getAppComponent().inject(this);
	}
	
	public void goUp() {
		try {
			TreeItem current = treeManager.getCurrentItem();
			TreeItem parent = current.getParent();
			treeManager.goUp();
			new GUIController().updateItemsList();
			new GUIController().restoreScrollPosition(parent);
		} catch (NoSuperItemException e) {
			new ExitController().saveAndExitRequested();
		}
	}
	
	public void itemGoIntoClicked(int position) {
		if (lock.isLocked()) {
			lock.setLocked(false);
			Logs.debug("Database unlocked.");
		}
		treeManager.selectionManager().cancelSelectionMode();
		goInto(position);
		new GUIController().updateItemsList();
		gui.scrollToItem(0);
	}
	
	public void goInto(int childIndex) {
		Integer scrollPos = gui.getCurrentScrollPos();
		if (scrollPos != null) {
			scrollCache.storeScrollPosition(treeManager.getCurrentItem(), scrollPos);
		}
		treeManager.goInto(childIndex);
	}
	
	
	public void itemLongClicked(int position) {
		if (!treeManager.selectionManager().isSelectionMode()) {
			treeManager.selectionManager().startSelectionMode();
			treeManager.selectionManager().setItemSelected(position, true);
			new GUIController().updateItemsList();
			gui.scrollToItem(position);
		} else {
			treeManager.selectionManager().setItemSelected(position, true);
			new GUIController().updateItemsList();
		}
	}
	
	public void itemClicked(int position, TreeItem item) {
		// blokada wejścia wgłąb na pierwszym poziomie poprzez kliknięcie
		if (lock.isLocked()) {
			Logs.warn("Database is locked.");
			return;
		}
		if (treeManager.selectionManager().isSelectionMode()) {
			treeManager.selectionManager().toggleItemSelected(position);
			new GUIController().updateItemsList();
		} else {
			if (item.isEmpty()) {
				new ItemEditorController().itemEditClicked(item);
			} else {
				itemGoIntoClicked(position);
			}
		}
	}
	
	public void itemMoved(int position, int step) {
		treeManager.mover().move(treeManager.getCurrentItem(), position, step);
	}
}
