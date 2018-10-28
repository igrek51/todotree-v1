package igrek.todotree.service.access;

import android.app.Activity;
import android.view.WindowManager;

import igrek.todotree.commands.ExitCommand;
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
		showOnLockScreen();
		setQuickAddMode(true);
		editNewTmpItem();
	}
	
	public void showOnLockScreen() {
		activity.getWindow()
				.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
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
	
	public void exitApp() {
		logger.debug("Exitting quick add mode...");
		new ExitCommand().optionSaveAndExit();
	}
}
