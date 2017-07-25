package igrek.todotree.dagger;

import android.app.Activity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import igrek.todotree.filesystem.FilesystemService;
import igrek.todotree.logic.backup.BackupManager;
import igrek.todotree.logic.datatree.TreeManager;
import igrek.todotree.logic.datatree.serializer.TreeSerializer;
import igrek.todotree.preferences.Preferences;
import igrek.todotree.resources.UserInfoService;

@Module
public class AppFactoryModule {
	
	private Activity activity;
	
	public AppFactoryModule(Activity activity) {
		this.activity = activity;
	}
	
	@Provides
	@Singleton
	Activity provideActivity() {
		return activity;
	}
	
	@Provides
	@Singleton
	FilesystemService provideFilesystemService(Activity activity) {
		return new FilesystemService(activity);
	}
	
	@Provides
	@Singleton
	TreeManager provideTreeManager() {
		return new TreeManager();
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
	UserInfoService provideUserInfoService(Activity activity) {
		return new UserInfoService(activity);
	}
	
	@Provides
	@Singleton
	TreeSerializer provideTreeSerializer() {
		return new TreeSerializer();
	}
	
}
