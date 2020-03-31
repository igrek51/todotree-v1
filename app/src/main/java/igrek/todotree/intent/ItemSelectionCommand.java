package igrek.todotree.intent;


import java.math.BigDecimal;
import java.util.Set;

import javax.inject.Inject;

import igrek.todotree.dagger.DaggerIoc;
import igrek.todotree.service.calc.NumericAdder;
import igrek.todotree.service.clipboard.SystemClipboardManager;
import igrek.todotree.service.resources.UserInfoService;
import igrek.todotree.service.tree.TreeManager;
import igrek.todotree.service.tree.TreeSelectionManager;

public class ItemSelectionCommand {
	
	@Inject
	TreeManager treeManager;
	
	@Inject
	UserInfoService userInfo;
	
	@Inject
	SystemClipboardManager clipboardManager;
	
	@Inject
	TreeSelectionManager selectionManager;
	
	public ItemSelectionCommand() {
		DaggerIoc.factoryComponent.inject(this);
	}
	
	private void selectAllItems(boolean selectedState) {
		for (int i = 0; i < treeManager.getCurrentItem().size(); i++) {
			selectionManager.setItemSelected(i, selectedState);
		}
		new GUICommand().updateItemsList();
	}
	
	public void toggleSelectAll() {
		if (selectionManager.getSelectedItemsCount() == treeManager.getCurrentItem().size()) {
			deselectAll();
		} else {
			selectAllItems(true);
		}
	}
	
	public void deselectAll() {
		selectionManager.cancelSelectionMode();
		new GUICommand().updateItemsList();
	}
	
	
	public void sumItems() {
		Set<Integer> itemIds;
		if (selectionManager.isAnythingSelected()) {
			itemIds = selectionManager.getSelectedItems();
		} else {
			itemIds = treeManager.getAllChildrenIds();
		}
		
		try {
			BigDecimal sum = new NumericAdder().calculateSum(itemIds, treeManager.getCurrentItem());
			
			String clipboardStr = sum.toPlainString();
			clipboardStr = clipboardStr.replace('.', ',');
			
			clipboardManager.copyToSystemClipboard(clipboardStr);
			userInfo.showInfo("Sum copied to clipboard: " + clipboardStr);
			
		} catch (NumberFormatException e) {
			userInfo.showInfo(e.getMessage());
		}
	}
	
	public void selectedItemClicked(int position, boolean checked) {
		selectionManager.setItemSelected(position, checked);
		new GUICommand().updateItemsList();
	}
	
}
