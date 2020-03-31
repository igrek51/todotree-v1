package igrek.todotree.intent;


import android.os.Handler;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.todotree.activity.ActivityController;
import igrek.todotree.app.AppData;
import igrek.todotree.app.AppState;
import igrek.todotree.dagger.DaggerIoc;
import igrek.todotree.info.logger.Logger;
import igrek.todotree.info.logger.LoggerFactory;
import igrek.todotree.service.access.DatabaseLock;
import igrek.todotree.service.preferences.Preferences;
import igrek.todotree.service.preferences.PropertyDefinition;
import igrek.todotree.service.summary.AlarmService;
import igrek.todotree.system.WindowManagerService;
import igrek.todotree.ui.GUI;

public class ExitCommand {
	
	@Inject
	AppData appData;
	@Inject
	GUI gui;
	@Inject
	Preferences preferences;
	@Inject
	AlarmService alarmService;
	@Inject
	DatabaseLock lock;
	@Inject
	ActivityController activityController;
	@Inject
	Lazy<WindowManagerService> windowManagerService;
	
	private Logger logger = LoggerFactory.INSTANCE.getLogger();
	
	public ExitCommand() {
		DaggerIoc.factoryComponent.inject(this);
	}
	
	void saveAndExitRequested() {
		// show exit screen and wait for rendered
		gui.showExitScreen();
		
		new Handler().post(this::quickSaveAndExit);
	}
	
	public void optionSaveAndExit() {
		if (appData.isState(AppState.EDIT_ITEM_CONTENT)) {
			gui.requestSaveEditedItem();
		}
		saveAndExitRequested();
	}
	
	void exitApp() {
		preferences.saveAll();
		activityController.quit();
	}
	
	private void saveAndExit() {
		new PersistenceCommand().saveDatabase();
		alarmService.setAlarmAt(alarmService.getNextMidnight());
		exitApp();
	}
	
	void quickSaveAndExit() {
		new Handler().post(this::postQuickSave);
		activityController.minimize();
		logger.info("Quick exiting...");
	}
	
	private void postQuickSave() {
		new PersistenceCommand().saveDatabase();
		alarmService.setAlarmAt(alarmService.getNextMidnight());
		preferences.saveAll();
		windowManagerService.get().keepScreenOn(false);
		
		new TreeCommand().navigateToRoot();
		boolean shouldBeLocked = preferences.getValue(PropertyDefinition.lockDB, Boolean.class);
		lock.setLocked(shouldBeLocked);
		new GUICommand().showItemsList();
		
		logger.debug("Quick exiting done");
	}
}
