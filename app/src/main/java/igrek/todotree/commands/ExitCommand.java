package igrek.todotree.commands;


import android.os.Handler;

import javax.inject.Inject;

import igrek.todotree.app.AppControllerService;
import igrek.todotree.app.AppData;
import igrek.todotree.app.AppState;
import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.logger.Logger;
import igrek.todotree.logger.LoggerFactory;
import igrek.todotree.service.access.DatabaseLock;
import igrek.todotree.service.preferences.Preferences;
import igrek.todotree.service.preferences.PropertyDefinition;
import igrek.todotree.service.summary.AlarmService;
import igrek.todotree.ui.GUI;

public class ExitCommand {
	
	@Inject
	AppData appData;
	@Inject
	GUI gui;
	@Inject
	Preferences preferences;
	@Inject
	AppControllerService appControllerService;
	@Inject
	AlarmService alarmService;
	@Inject
	DatabaseLock lock;
	
	private Logger logger = LoggerFactory.getLogger();
	
	public ExitCommand() {
		DaggerIOC.getFactoryComponent().inject(this);
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
		appControllerService.quit();
	}
	
	private void saveAndExit() {
		new PersistenceCommand().saveDatabase();
		alarmService.setAlarmAt(alarmService.getNextMidnight());
		exitApp();
	}
	
	void quickSaveAndExit() {
		new Handler().post(this::postQuickSave);
		appControllerService.minimize();
		logger.info("Quick exiting...");
	}
	
	private void postQuickSave() {
		new PersistenceCommand().saveDatabase();
		alarmService.setAlarmAt(alarmService.getNextMidnight());
		preferences.saveAll();
		appControllerService.stopKeepingScreenOn();
		
		new TreeCommand().navigateToRoot();
		boolean shouldBeLocked = preferences.getValue(PropertyDefinition.lockDB, Boolean.class);
		lock.setLocked(shouldBeLocked);
		new GUICommand().showItemsList();
		
		logger.debug("Quick exiting done");
	}
}
