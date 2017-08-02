package igrek.todotree.dagger;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import igrek.todotree.app.App;
import igrek.todotree.app.AppData;
import igrek.todotree.datatree.ContentTrimmer;
import igrek.todotree.datatree.TreeClipboardManager;
import igrek.todotree.datatree.TreeManager;
import igrek.todotree.datatree.TreeMover;
import igrek.todotree.datatree.TreeScrollCache;
import igrek.todotree.datatree.TreeSelectionManager;
import igrek.todotree.datatree.serializer.TreeSerializer;
import igrek.todotree.gui.GUI;
import igrek.todotree.services.backup.BackupManager;
import igrek.todotree.services.clipboard.SystemClipboardManager;
import igrek.todotree.services.filesystem.FilesystemService;
import igrek.todotree.services.history.ChangesHistory;
import igrek.todotree.services.lock.DatabaseLock;
import igrek.todotree.services.preferences.Preferences;
import igrek.todotree.services.resources.UserInfoService;

@Module
public class AppFactoryModule {
	
	private App app;
	private Activity activity;
	
	public AppFactoryModule(App app, Activity activity) {
		this.app = app;
		this.activity = activity;
	}
	
	@Provides
	@Singleton
	App provideApp() {
		return app;
	}
	
	@Provides
	@Singleton
	Activity provideActivity() {
		return activity;
	}
	
	@Provides
	@Singleton
	AppCompatActivity provideAppCompatActivity() {
		return (AppCompatActivity) activity;
	}
	
	@Provides
	@Singleton
	FilesystemService provideFilesystemService(Activity activity) {
		return new FilesystemService(activity);
	}
	
	@Provides
	@Singleton
	TreeManager provideTreeManager(ChangesHistory changesHistory) {
		return new TreeManager(changesHistory);
	}
	
	@Provides
	@Singleton
	Preferences providePreferences(Activity activity) {
		return new Preferences(activity);
	}
	
	@Provides
	@Singleton
	BackupManager provideBackupManager(Preferences preferences, FilesystemService filesystem) {
		return new BackupManager(preferences, filesystem);
	}
	
	@Provides
	@Singleton
	UserInfoService provideUserInfoService(Activity activity, GUI gui) {
		return new UserInfoService(activity, gui);
	}
	
	@Provides
	@Singleton
	TreeSerializer provideTreeSerializer() {
		return new TreeSerializer();
	}
	
	@Provides
	@Singleton
	GUI provideGUI(AppCompatActivity activity) {
		return new GUI(activity);
	}
	
	@Provides
	@Singleton
	SystemClipboardManager provideSystemClipboardManager(Activity activity) {
		return new SystemClipboardManager(activity);
	}
	
	@Provides
	@Singleton
	AppData provideAppData() {
		return new AppData();
	}
	
	@Provides
	@Singleton
	DatabaseLock provideDatabaseLock() {
		return new DatabaseLock();
	}
	
	@Provides
	@Singleton
	ChangesHistory provideChangesHistory() {
		return new ChangesHistory();
	}
	
	@Provides
	@Singleton
	ContentTrimmer provideContentTrimmer() {
		return new ContentTrimmer();
	}
	
	@Provides
	@Singleton
	TreeScrollCache provideTreeScrollCache() {
		return new TreeScrollCache();
	}
	
	@Provides
	@Singleton
	TreeClipboardManager provideTreeClipboardManager() {
		return new TreeClipboardManager();
	}
	
	@Provides
	@Singleton
	TreeSelectionManager provideTreeSelectionManager() {
		return new TreeSelectionManager();
	}
	
	@Provides
	@Singleton
	TreeMover provideTreeMover() {
		return new TreeMover();
	}
	
}
