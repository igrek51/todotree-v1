package igrek.todotree.intent;


import org.joda.time.DateTime;

import javax.inject.Inject;

import igrek.todotree.R;
import igrek.todotree.activity.ActivityController;
import igrek.todotree.app.AppData;
import igrek.todotree.app.AppState;
import igrek.todotree.dagger.DaggerIoc;
import igrek.todotree.service.summary.AlarmService;
import igrek.todotree.service.summary.NotificationService;
import igrek.todotree.service.tree.TreeManager;
import igrek.todotree.service.tree.TreeSelectionManager;
import igrek.todotree.ui.GUI;

public class NavigationCommand {
	
	@Inject
	TreeManager treeManager;
	
	@Inject
	GUI gui;
	
	@Inject
	ActivityController activityController;
	
	@Inject
	AppData appData;
	
	@Inject
	TreeSelectionManager selectionManager;
	
	@Inject
	NotificationService notificationService;
	
	@Inject
	AlarmService alarmService;
	
	public NavigationCommand() {
		DaggerIoc.factoryComponent.inject(this);
	}
	
	public boolean optionsSelect(int id) {
		switch (id) {
			case R.id.action_minimize:
				activityController.minimize();
				return true;
			case R.id.action_exit_without_saving:
				new ExitCommand().exitApp();
				return true;
			case R.id.action_save_exit:
				new ExitCommand().optionSaveAndExit();
				return true;
			case R.id.action_save:
				new PersistenceCommand().optionSave();
				return true;
			case R.id.action_reload:
				new PersistenceCommand().optionReload();
				return true;
			case R.id.action_restore_backup:
				new PersistenceCommand().optionRestoreBackup();
				return true;
			case R.id.action_select_all:
				new ItemSelectionCommand().toggleSelectAll();
				return false;
			case R.id.action_cut:
				new ClipboardCommand().cutSelectedItems();
				return false;
			case R.id.action_copy:
				new ClipboardCommand().copySelectedItems();
				return false;
			case R.id.action_sum_selected:
				new ItemSelectionCommand().sumItems();
				return false;
			case R.id.action_show_statistics:
				new StatisticsCommand().showStatisticsInfo();
				return false;
			case R.id.action_go_up:
				new TreeCommand().goUp();
				return false;
			case R.id.action_notify:
				summaryNotify();
				return false;
		}
		return false;
	}
	
	private void summaryNotify() {
		alarmService.setAlarmAt(DateTime.now().plusSeconds(10));
	}
	
	public boolean backClicked() {
		if (appData.isState(AppState.ITEMS_LIST)) {
			if (selectionManager.isAnythingSelected()) {
				selectionManager.cancelSelectionMode();
				new GUICommand().updateItemsList();
			} else {
				new TreeCommand().goBack();
			}
		} else if (appData.isState(AppState.EDIT_ITEM_CONTENT)) {
			if (gui.editItemBackClicked())
				return true;
			new ItemEditorCommand().cancelEditedItem();
		}
		return true;
	}
	
	public boolean approveClicked() {
		if (appData.isState(AppState.EDIT_ITEM_CONTENT)) {
			gui.requestSaveEditedItem();
		} else if (appData.isState(AppState.ITEMS_LIST)) {
			if (selectionManager.isAnythingSelected()) {
				selectionManager.cancelSelectionMode();
				new GUICommand().updateItemsList();
			} else {
				new TreeCommand().goBack();
			}
		}
		return true;
	}
	
}
