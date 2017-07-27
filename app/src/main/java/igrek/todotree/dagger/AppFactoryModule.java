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
	
	//	SERVICES
	
	@Provides
	@Singleton
	FilesystemService provideFilesystemService() {
		return new FilesystemService();
	}
	
	@Provides
	@Singleton
	TreeManager provideTreeManager() {
		return new TreeManager();
	}
	
	@Provides
	@Singleton
	Preferences providePreferences() {
		return new Preferences();
	}
	
	@Provides
	@Singleton
	BackupManager provideBackupManager() {
		return new BackupManager();
	}
	
	@Provides
	@Singleton
	UserInfoService provideUserInfoService() {
		return new UserInfoService();
	}
	
	@Provides
	@Singleton
	TreeSerializer provideTreeSerializer() {
		return new TreeSerializer();
	}
	
	@Provides
	@Singleton
	LogicActionController provideLogicActionController() {
		return new LogicActionController();
	}
	
	@Provides
	@Singleton
	GUI provideGUI() {
		return new GUI();
	}
	
	@Provides
	@Singleton
	ClipboardManager provideClipboardManager() {
		return new ClipboardManager();
	}
	
}
