package igrek.todotree.commands;


import android.os.Handler;

import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.model.treeitem.AbstractTreeItem;
import igrek.todotree.services.resources.UserInfoService;
import igrek.todotree.services.tree.TreeManager;
import igrek.todotree.services.tree.TreeSelectionManager;
import igrek.todotree.ui.GUI;

public class ItemActionCommand {
	
	@Inject
	TreeManager treeManager;
	
	@Inject
	GUI gui;
	
	@Inject
	TreeSelectionManager selectionManager;
	
	@Inject
	UserInfoService userInfoService;
	
	public ItemActionCommand() {
		DaggerIOC.getAppComponent().inject(this);
	}
	
	public void actionSelect(int position) {
		if (treeManager.isPositionBeyond(position)) {
			userInfoService.showInfo("Could not select");
		} else {
			//tryb zaznaczania elementów
			new TreeCommand().itemLongClicked(position);
		}
	}
	
	public void actionAddAbove(final int position) {
		//delayed execution due to not showing keyboard
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				new ItemEditorCommand().addItemHereClicked(position);
			}
		});
	}
	
	public void actionCopy(int position) {
		Set<Integer> itemPosistions = new TreeSet<>(selectionManager.getSelectedItemsNotNull());
		// if nothing selected - include current item
		if (itemPosistions.isEmpty()) {
			itemPosistions.add(position);
		}
		new ClipboardCommand().copyItems(itemPosistions, true);
	}
	
	public void actionPasteAbove(final int position) {
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				new ClipboardCommand().pasteItems(position);
			}
		});
	}
	
	public void actionPasteAboveAsLink(final int position) {
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				new ClipboardCommand().pasteItemsAsLink(position);
			}
		});
	}
	
	public void actionCut(int position) {
		TreeSet<Integer> itemPosistions = new TreeSet<>(selectionManager.getSelectedItemsNotNull());
		// if nothing selected - include current item
		if (itemPosistions.isEmpty()) {
			itemPosistions.add(position);
		}
		new ClipboardCommand().cutItems(itemPosistions);
	}
	
	public void actionRemove(int position) {
		new ItemTrashCommand().itemRemoveClicked(position);
	}
	
	public void actionSelectAll(int position) {
		new ItemSelectionCommand().toggleSelectAll();
	}
	
	public void actionEdit(final int position) {
		//delayed execution due to not showing keyboard
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				AbstractTreeItem item = treeManager.getCurrentItem().getChild(position);
				new ItemEditorCommand().itemEditClicked(item);
			}
		});
	}
}