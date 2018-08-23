package igrek.todotree.service.access;

import android.app.Activity;

import igrek.todotree.commands.ItemEditorCommand;
import igrek.todotree.domain.treeitem.AbstractTreeItem;
import igrek.todotree.logger.Logger;
import igrek.todotree.service.tree.TreeManager;

public class QuickAddService {
	
	private Logger logger;
	private Activity activity;
	private TreeManager treeManager;
	
	private boolean quickAddMode = false;
	
	public QuickAddService(Logger logger, Activity activity, TreeManager treeManager) {
		this.logger = logger;
		this.activity = activity;
		this.treeManager = treeManager;
	}
	
	public boolean isQuickAddMode() {
		return quickAddMode;
	}
	
	public void setQuickAddMode(boolean quickAddMode) {
		this.quickAddMode = quickAddMode;
	}
	
	public void initQuickAdd() {
		setQuickAddMode(true);
		editNewTmpItem();
	}
	
	private void editNewTmpItem() {
		// go to Tmp
		AbstractTreeItem tmpItem = treeManager.getCurrentItem().findChildByName("Tmp");
		if (tmpItem == null) {
			logger.error("Tmp item was not found");
			return;
		}
		treeManager.goTo(tmpItem);
		// add item at the end
		new ItemEditorCommand().addItemClicked();
	}
}
