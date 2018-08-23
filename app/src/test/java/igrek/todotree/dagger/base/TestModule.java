package igrek.todotree.dagger.base;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;

import org.mockito.Mockito;

import igrek.todotree.MainApplication;
import igrek.todotree.dagger.FactoryModule;
import igrek.todotree.logger.Logger;
import igrek.todotree.mock.LoggerMock;
import igrek.todotree.mock.MockedExternalCardService;
import igrek.todotree.mock.MockedFilesystemService;
import igrek.todotree.mock.MockedGUI;
import igrek.todotree.mock.MockedPreferences;
import igrek.todotree.mock.MockedSystemClipboardManager;
import igrek.todotree.mock.MockedUserInfoService;
import igrek.todotree.service.clipboard.SystemClipboardManager;
import igrek.todotree.service.filesystem.ExternalCardService;
import igrek.todotree.service.filesystem.FilesystemService;
import igrek.todotree.service.preferences.Preferences;
import igrek.todotree.service.resources.UserInfoService;
import igrek.todotree.ui.GUI;

public class TestModule extends FactoryModule {
	
	public TestModule(MainApplication application) {
		super(application.getCurrentActivity());
	}
	
	@Override
	protected Logger provideLogger() {
		return new LoggerMock();
	}
	
	@Override
	protected AppCompatActivity provideAppCompatActivity() {
		return Mockito.mock(AppCompatActivity.class);
	}
	
	
	@Override
	protected UserInfoService provideUserInfoService(Activity activity, GUI gui, Logger logger) {
		return new MockedUserInfoService(activity, gui, logger);
	}
	
	@Override
	protected FilesystemService provideFilesystemService(Logger logger, Activity activity, ExternalCardService externalCardService) {
		return new MockedFilesystemService(logger, activity, externalCardService);
	}
	
	@Override
	protected ExternalCardService provideExternalCardService(Logger logger, Activity activity) {
		return new MockedExternalCardService(logger, activity);
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
	protected Preferences providePreferences(Activity activity, Logger logger) {
		return new MockedPreferences(activity, logger);
	}
}
