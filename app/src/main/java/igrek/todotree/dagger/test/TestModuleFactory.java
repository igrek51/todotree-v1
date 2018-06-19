package igrek.todotree.dagger.test;


import android.app.Activity;
import android.support.v7.app.AppCompatActivity;

import igrek.todotree.app.App;
import igrek.todotree.dagger.AppFactoryModule;
import igrek.todotree.logger.Logs;
import igrek.todotree.mock.MockedExternalCardService;
import igrek.todotree.mock.MockedFilesystemService;
import igrek.todotree.mock.MockedGUI;
import igrek.todotree.mock.MockedLogs;
import igrek.todotree.mock.MockedPreferences;
import igrek.todotree.mock.MockedSystemClipboardManager;
import igrek.todotree.mock.MockedUserInfoService;
import igrek.todotree.services.clipboard.SystemClipboardManager;
import igrek.todotree.services.filesystem.ExternalCardService;
import igrek.todotree.services.filesystem.FilesystemService;
import igrek.todotree.services.preferences.Preferences;
import igrek.todotree.services.resources.UserInfoService;
import igrek.todotree.ui.GUI;

public class TestModuleFactory {
	
	public static AppFactoryModule getTestModule(App app, Activity activity) {
		return new AppFactoryModule(app, activity) {
			
			@Override
			protected UserInfoService provideUserInfoService(Activity activity, GUI gui, Logs logger) {
				return new MockedUserInfoService(activity, gui, logger);
			}
			
			@Override
			protected FilesystemService provideFilesystemService(Logs logger, Activity activity, ExternalCardService externalCardService) {
				return new MockedFilesystemService(logger, activity, externalCardService);
			}
			
			@Override
			protected ExternalCardService provideExternalCardService(Logs logger, Activity activity) {
				return new MockedExternalCardService(logger, activity);
			}
			
			@Override
			protected Logs provideLogger() {
				return new MockedLogs();
			}
			
			@Override
			protected SystemClipboardManager provideSystemClipboardManager(Activity activity) {
				return new MockedSystemClipboardManager(activity);
			}
			
			@Override
			protected GUI provideGUI(AppCompatActivity activity) {
				return new MockedGUI(activity);
			}
			
			@Override
			protected Preferences providePreferences(Activity activity, Logs logger) {
				return new MockedPreferences(activity, logger);
			}
		};
	}
}
