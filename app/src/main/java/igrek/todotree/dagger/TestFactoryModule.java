package igrek.todotree.dagger;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import igrek.todotree.app.App;
import igrek.todotree.app.AppData;
import igrek.todotree.services.backup.BackupManager;
import igrek.todotree.services.clipboard.SystemClipboardManager;
import igrek.todotree.services.clipboard.TreeClipboardManager;
import igrek.todotree.services.filesystem.FilesystemService;
import igrek.todotree.services.history.ChangesHistory;
import igrek.todotree.services.lock.DatabaseLock;
import igrek.todotree.services.preferences.Preferences;
import igrek.todotree.services.resources.UserInfoService;
import igrek.todotree.services.tree.ContentTrimmer;
import igrek.todotree.services.tree.TreeManager;
import igrek.todotree.services.tree.TreeMover;
import igrek.todotree.services.tree.TreeScrollCache;
import igrek.todotree.services.tree.TreeSelectionManager;
import igrek.todotree.services.tree.serializer.JsonTreeSerializer;
import igrek.todotree.ui.GUI;

@Module
public class TestFactoryModule {
	
	public TestFactoryModule() {
	}
	
	@Provides
	@Singleton
	@Nullable
	App provideApp() {
		return null;
	}
	
	@Provides
	@Singleton
	@Nullable
	Activity provideActivity() {
		return null;
	}
	
	@Provides
	@Singleton
	@Nullable
	AppCompatActivity provideAppCompatActivity() {
		return null;
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
	JsonTreeSerializer provideTreeSerializer() {
		return new JsonTreeSerializer();
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
