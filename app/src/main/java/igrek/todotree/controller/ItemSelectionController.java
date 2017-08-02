package igrek.todotree.controller;


import java.math.BigDecimal;

import javax.inject.Inject;

import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.datatree.NumericAdder;
import igrek.todotree.datatree.TreeManager;
import igrek.todotree.datatree.TreeSelectionManager;
import igrek.todotree.services.clipboard.SystemClipboardManager;
import igrek.todotree.services.resources.UserInfoService;

public class ItemSelectionController {
	
	@Inject
	TreeManager treeManager;
	
	@Inject
	UserInfoService userInfo;
	
	@Inject
	SystemClipboardManager clipboardManager;
	
	@Inject
	TreeSelectionManager selectionManager;
	
	public ItemSelectionController() {
		DaggerIOC.getAppComponent().inject(this);
	}
	
	private void selectAllItems(boolean selectedState) {
		for (int i = 0; i < treeManager.getCurrentItem().size(); i++) {
			selectionManager.setItemSelected(i, selectedState);
		}
	}
	
	public void toggleSelectAll() {
		if (selectionManager.getSelectedItemsCount() == treeManager.getCurrentItem().size()) {
			selectionManager.cancelSelectionMode();
		} else {
			selectAllItems(true);
		}
		new GUIController().updateItemsList();
	}
	
	
	public void sumSelected() {
		if (selectionManager.isAnythingSelected()) {
			try {
				BigDecimal sum = calculateSum();
				
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
		selectionManager.setItemSelected(position, checked);
		new GUIController().updateItemsList();
	}
	
	private BigDecimal calculateSum() {
		return new NumericAdder().calculateSum(selectionManager.getSelectedItems(), treeManager.getCurrentItem());
	}
	
}
