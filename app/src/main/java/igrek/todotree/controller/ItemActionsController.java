package igrek.todotree.controller;


import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.datatree.TreeManager;
import igrek.todotree.datatree.TreeSelectionManager;
import igrek.todotree.datatree.item.TreeItem;
import igrek.todotree.gui.GUI;
import igrek.todotree.services.resources.UserInfoService;

public class ItemActionsController {
	
	@Inject
	TreeManager treeManager;
	
	@Inject
	GUI gui;
	
	@Inject
	TreeSelectionManager selectionManager;
	
	@Inject
	UserInfoService userInfoService;
	
	public ItemActionsController() {
		DaggerIOC.getAppComponent().inject(this);
	}
	
	public void actionSelect(int position) {
		if (treeManager.isPositionBeyond(position)) {
			userInfoService.showInfo("Could not select");
		} else {
			//tryb zaznaczania elementów
			new TreeController().itemLongClicked(position);
		}
	}
	
	public void actionAddAbove(final int position) {
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				new ItemEditorController().addItemHereClicked(position);
			}
		});
	}
	
	public void actionCopy(int position) {
		List<Integer> itemPosistions = new ArrayList<>(selectionManager.getSelectedItems());
		// if nothing selected - include current item
		if (itemPosistions.isEmpty()) {
			itemPosistions.add(position);
		}
		new ClipboardController().copyItems(itemPosistions, true);
	}
	
	public void actionPasteAbove(int position) {
		new ClipboardController().pasteItems(position);
	}
	
	public void actionPasteAboveAsLink(int position) {
		// TODO
	}
	
	public void actionCut(int position) {
		List<Integer> itemPosistions = new ArrayList<>(selectionManager.getSelectedItems());
		// if nothing selected - include current item
		if (itemPosistions.isEmpty()) {
			itemPosistions.add(position);
		}
		new ClipboardController().cutItems(itemPosistions);
	}
	
	public void actionRemove(int position) {
		new ItemTrashController().itemRemoveClicked(position);
	}
	
	public void actionSelectAll(int position) {
		new ItemSelectionController().toggleSelectAll();
	}
	
	public void actionEdit(int position) {
		TreeItem item = treeManager.getCurrentItem().getChild(position);
		new ItemEditorController().itemEditClicked(item);
	}
}
