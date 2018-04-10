package igrek.todotree.commands;


import android.os.Handler;

import javax.inject.Inject;

import igrek.todotree.app.App;
import igrek.todotree.app.AppData;
import igrek.todotree.app.AppState;
import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.services.preferences.Preferences;
import igrek.todotree.ui.GUI;

public class ExitCommand {
	
	@Inject
	AppData appData;
	
	@Inject
	GUI gui;
	
	@Inject
	Preferences preferences;
	
	@Inject
	App app;
	
	ExitCommand() {
		DaggerIOC.getAppComponent().inject(this);
	}
	
	void saveAndExitRequested() {
		// show exit screen and wait for rendered
		gui.showExitScreen();
		
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				saveAndExit();
			}
		});
	}
	
	void optionSaveAndExit() {
		if (appData.isState(AppState.EDIT_ITEM_CONTENT)) {
			gui.requestSaveEditedItem();
		}
		saveAndExitRequested();
	}
	
	void exitApp() {
		preferences.saveAll();
		app.quit();
	}
	
	private void saveAndExit() {
		new PersistenceCommand().saveDatabase();
		exitApp();
	}
}