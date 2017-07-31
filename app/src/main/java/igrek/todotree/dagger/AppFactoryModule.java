package igrek.todotree.dagger;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import igrek.todotree.filesystem.FilesystemService;
import igrek.todotree.gui.GUI;
import igrek.todotree.logic.ClipboardManager;
import igrek.todotree.logic.LogicActionController;
import igrek.todotree.logic.app.App;
import igrek.todotree.logic.backup.BackupManager;
import igrek.todotree.logic.datatree.TreeManager;
import igrek.todotree.logic.datatree.serializer.TreeSerializer;
import igrek.todotree.preferences.Preferences;
import igrek.todotree.resources.UserInfoService;

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
	LogicActionController provideLogicActionController(TreeManager treeManager, BackupManager backupManager, GUI gui, UserInfoService userInfo, ClipboardManager clipboardManager, App app) {
		return new LogicActionController(treeManager, backupManager, gui, userInfo, clipboardManager, app);
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
	
}