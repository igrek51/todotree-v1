package igrek.todotree.logic;


import igrek.todotree.R;
import igrek.todotree.gui.GUI;
import igrek.todotree.logic.app.App;
import igrek.todotree.logic.app.AppState;
import igrek.todotree.logic.datatree.TreeManager;
import igrek.todotree.resources.UserInfoService;

public class LogicActionController {
	
	private TreeManager treeManager;
	private GUI gui;
	private UserInfoService userInfo;
	private App app;
	
	public LogicActionController(TreeManager treeManager, GUI gui, UserInfoService userInfo, App app) {
		this.treeManager = treeManager;
		this.gui = gui;
		this.userInfo = userInfo;
		this.app = app;
	}
	
	public boolean optionsSelect(int id) {
		if (id == R.id.action_minimize) {
			app.minimize();
			return true;
		} else if (id == R.id.action_exit_without_saving) {
			app.exitApp(false);
			return true;
		} else if (id == R.id.action_save_exit) {
			if (app.getState() == AppState.EDIT_ITEM_CONTENT) {
				gui.requestSaveEditedItem();
			}
			app.exitApp(true);
			return true;
		}
		// TODO fix options
		//		} else if (id == R.id.action_save) {
		//			saveDatabase();
		//			userInfo.showInfo("Zapisano bazę danych.");
		//			return true;
		//		} else if (id == R.id.action_reload) {
		//			treeManager.reset();
		//			treeManager.loadRootTree();
		//			updateItemsList();
		//			userInfo.showInfo("Wczytano bazę danych.");
		//			return true;
		//		} else if (id == R.id.action_copy) {
		//			copySelectedItems(true);
		//		} else if (id == R.id.action_cut) {
		//			cutSelectedItems();
		//		} else if (id == R.id.action_paste) {
		//			pasteItems();
		//		} else if (id == R.id.action_select_all) {
		//			toggleSelectAll();
		//		} else if (id == R.id.action_sum_selected) {
		//			sumSelected();
		//		}
		return false;
	}
}
