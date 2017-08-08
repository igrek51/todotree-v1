package igrek.todotree.dagger.test;


import android.app.Activity;

import igrek.todotree.app.App;
import igrek.todotree.dagger.AppFactoryModule;
import igrek.todotree.logger.Logs;
import igrek.todotree.mock.MockedLogs;
import igrek.todotree.mock.MockedUserInfoService;
import igrek.todotree.services.resources.UserInfoService;
import igrek.todotree.ui.GUI;

public class TestModuleFactory {
	
	public static AppFactoryModule getTestModule(App app, Activity activity) {
		return new AppFactoryModule(app, activity){
			
			@Override
			protected UserInfoService provideUserInfoService(Activity activity, GUI gui, Logs logger) {
				return new MockedUserInfoService(activity, gui, logger);
			}
			
			@Override
			protected Logs provideLogger() {
				return new MockedLogs();
			}
		};
	}
}
