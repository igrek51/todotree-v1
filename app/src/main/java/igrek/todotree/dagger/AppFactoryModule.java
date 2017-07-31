package igrek.todotree.dagger;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import igrek.todotree.app.App;
import igrek.todotree.app.AppData;
import igrek.todotree.controller.LogicActionController;
import igrek.todotree.gui.GUI;
import igrek.todotree.services.backup.BackupManager;
import igrek.todotree.services.clipboard.ClipboardManager;
import igrek.todotree.services.datatree.TreeManager;
import igrek.todotree.services.datatree.serializer.TreeSerializer;
import igrek.todotree.services.filesystem.FilesystemService;
import igrek.todotree.services.lock.DatabaseLock;
import igrek.todotree.services.preferences.Preferences;
import igrek.todotree.services.resources.resources.UserInfoService;

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
	TreeManager provideTreeManager(FilesystemService filesystem, Preferences preferences, TreeSerializer treeSerializer) {
		return new TreeManager(filesystem, preferences, treeSerializer);
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
	LogicActionController provideLogicActionController(TreeManager treeManager, BackupManager backupManager, GUI gui, UserInfoService userInfo, ClipboardManager clipboardManager, Preferences preferences, App app, AppData appData, DatabaseLock lock) {
		return new LogicActionController(treeManager, backupManager, gui, userInfo, clipboardManager, preferences, app, appData, lock);
	}
	
	@Provides
	@Singleton
	GUI provideGUI(AppCompatActivity activity) {
		return new GUI(activity);
	}
	
	@Provides
	@Singleton
	ClipboardManager provideClipboardManager(Activity activity) {
		return new ClipboardManager(activity);
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
	
}
