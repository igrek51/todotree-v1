package igrek.todotree.controller;


import java.math.BigDecimal;

import javax.inject.Inject;

import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.datatree.TreeManager;
import igrek.todotree.services.clipboard.ClipboardManager;
import igrek.todotree.services.resources.UserInfoService;

public class ItemSelectionController {
	
	@Inject
	TreeManager treeManager;
	
	@Inject
	UserInfoService userInfo;
	
	@Inject
	ClipboardManager clipboardManager;
	
	public ItemSelectionController() {
		DaggerIOC.getAppComponent().inject(this);
	}
	
	private void selectAllItems(boolean selectedState) {
		for (int i = 0; i < treeManager.getCurrentItem().size(); i++) {
			treeManager.selectionManager().setItemSelected(i, selectedState);
		}
	}
	
	public void toggleSelectAll() {
		if (treeManager.selectionManager().getSelectedItemsCount() == treeManager.getCurrentItem()
				.size()) {
			treeManager.selectionManager().cancelSelectionMode();
		} else {
			selectAllItems(true);
		}
		new GUIController().updateItemsList();
	}
	
	
	public void sumSelected() {
		if (treeManager.selectionManager().isSelectionMode()) {
			try {
				BigDecimal sum = treeManager.sumSelected();
				
				String clipboardStr = sum.toPlainString();
				clipboardStr = clipboardStr.replace('.', ',');
				
				clipboardManager.copyToSystemClipboard(clipboardStr);
				userInfo.showInfo("Sum copied to clipboard: " + clipboardStr);
				
			} catch (NumberFormatException e) {
				userInfo.showInfo(e.getMessage());
			}
		}
	}
	
	public void selectedItemClicked(int position, boolean checked) {
		treeManager.selectionManager().setItemSelected(position, checked);
		new GUIController().updateItemsList();
	}
	
}
