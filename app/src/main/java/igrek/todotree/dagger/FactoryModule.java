package igrek.todotree.dagger;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Singleton;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import igrek.todotree.app.AppControllerService;
import igrek.todotree.app.AppData;
import igrek.todotree.logger.Logger;
import igrek.todotree.logger.LoggerFactory;
import igrek.todotree.service.access.AccessLogService;
import igrek.todotree.service.access.DatabaseLock;
import igrek.todotree.service.access.QuickAddService;
import igrek.todotree.service.backup.BackupManager;
import igrek.todotree.service.clipboard.SystemClipboardManager;
import igrek.todotree.service.clipboard.TreeClipboardManager;
import igrek.todotree.service.commander.SecretCommander;
import igrek.todotree.service.filesystem.ExternalCardService;
import igrek.todotree.service.filesystem.FilesystemService;
import igrek.todotree.service.history.ChangesHistory;
import igrek.todotree.service.history.LinkHistoryService;
import igrek.todotree.service.preferences.Preferences;
import igrek.todotree.service.resources.UserInfoService;
import igrek.todotree.service.statistics.StatisticsLogService;
import igrek.todotree.service.summary.AlarmService;
import igrek.todotree.service.summary.DailySummaryService;
import igrek.todotree.service.summary.NotificationService;
import igrek.todotree.service.system.SoftKeyboardService;
import igrek.todotree.service.tree.ContentTrimmer;
import igrek.todotree.service.tree.TreeManager;
import igrek.todotree.service.tree.TreeMover;
import igrek.todotree.service.tree.TreeScrollCache;
import igrek.todotree.service.tree.TreeSelectionManager;
import igrek.todotree.service.tree.persistence.TreePersistenceService;
import igrek.todotree.ui.GUI;

/**
 * Module with providers. These classes can be injected
 */
@Module
public class FactoryModule {
	
	private AppCompatActivity activity;
	
	public FactoryModule(AppCompatActivity activity) {
		this.activity = activity;
	}
	
	@Provides
	protected AppCompatActivity provideAppCompatActivity() {
		return activity;
	}
	
	@Provides
	protected Activity provideActivity() {
		return activity;
	}
	
	@Provides
	protected Context provideContext() {
		return activity;
	}
	
	@Provides
	protected Logger provideLogger() {
		return LoggerFactory.getLogger();
	}
	
	@Provides
	@Singleton
	protected AppControllerService provideAppControllerSerivce(Activity activity) {
		return new AppControllerService(activity);
	}
	
	@Provides
	@Singleton
	protected FilesystemService provideFilesystemService(Logger logger, Activity activity, ExternalCardService externalCardService) {
		return new FilesystemService(logger, activity, externalCardService);
	}
	
	@Provides
	@Singleton
	protected TreeManager provideTreeManager(ChangesHistory changesHistory) {
		return new TreeManager(changesHistory);
	}
	
	@Provides
	@Singleton
	protected Preferences providePreferences(Context context, Logger logger) {
		return new Preferences(context, logger);
	}
	
	@Provides
	@Singleton
	protected BackupManager provideBackupManager(Preferences preferences, FilesystemService filesystem, Logger logger) {
		return new BackupManager(preferences, filesystem, logger);
	}
	
	@Provides
	@Singleton
	protected UserInfoService provideUserInfoService(Activity activity, GUI gui, Logger logger) {
		return new UserInfoService(activity, gui, logger);
	}
	
	@Provides
	@Singleton
	protected TreePersistenceService providePersistenceService() {
		return new TreePersistenceService();
	}
	
	@Provides
	@Singleton
	protected GUI provideGUI(AppCompatActivity activity) {
		return new GUI(activity);
	}
	
	@Provides
	@Singleton
	protected SystemClipboardManager provideSystemClipboardManager(Activity activity) {
		return new SystemClipboardManager(activity);
	}
	
	@Provides
	@Singleton
	protected AppData provideAppData() {
		return new AppData();
	}
	
	@Provides
	@Singleton
	protected DatabaseLock provideDatabaseLock(Preferences preferences, Logger logger, AccessLogService accessLogService) {
		return new DatabaseLock(preferences, logger, accessLogService);
	}
	
	@Provides
	@Singleton
	protected AccessLogService provideAccessLogService(FilesystemService filesystem, Preferences preferences, Logger logger) {
		return new AccessLogService(filesystem, preferences, logger);
	}
	
	@Provides
	@Singleton
	protected ChangesHistory provideChangesHistory() {
		return new ChangesHistory();
	}
	
	@Provides
	@Singleton
	protected ContentTrimmer provideContentTrimmer() {
		return new ContentTrimmer();
	}
	
	@Provides
	@Singleton
	protected TreeScrollCache provideTreeScrollCache() {
		return new TreeScrollCache();
	}
	
	@Provides
	@Singleton
	protected TreeClipboardManager provideTreeClipboardManager() {
		return new TreeClipboardManager();
	}
	
	@Provides
	@Singleton
	protected TreeSelectionManager provideTreeSelectionManager() {
		return new TreeSelectionManager();
	}
	
	@Provides
	@Singleton
	protected TreeMover provideTreeMover() {
		return new TreeMover();
	}
	
	@Provides
	@Singleton
	protected SecretCommander provideCommander(Logger logger, Preferences preferences, UserInfoService userInfo) {
		return new SecretCommander(logger, preferences, userInfo);
	}
	
	@Provides
	@Singleton
	protected StatisticsLogService provideStatisticsLogService(FilesystemService filesystem, Preferences preferences) {
		return new StatisticsLogService(filesystem, preferences);
	}
	
	@Provides
	@Singleton
	protected ExternalCardService provideExternalCardService(Logger logger) {
		return new ExternalCardService(logger);
	}
	
	@Provides
	@Singleton
	protected QuickAddService provideQuickAddService(Logger logger, Activity activity, TreeManager treeManager, SoftKeyboardService softKeyboardService, Lazy<GUI> gui) {
		return new QuickAddService(logger, activity, treeManager, softKeyboardService, gui);
	}
	
	@Provides
	@Singleton
	protected LinkHistoryService provideLinkHistoryService() {
		return new LinkHistoryService();
	}
	
	@Provides
	@Singleton
	protected NotificationService provideNotificationService() {
		return new NotificationService();
	}
	
	@Provides
	@Singleton
	protected AlarmService provideAlarmService(Activity activity) {
		return new AlarmService(activity);
	}
	
	@Provides
	@Singleton
	protected SoftKeyboardService provideSoftKeyboardService(Activity activity) {
		return new SoftKeyboardService(activity);
	}
	
	@Provides
	@Singleton
	protected DailySummaryService provideDailySummaryService(Activity activity, NotificationService notificationService, StatisticsLogService statisticsLogService) {
		return new DailySummaryService(activity, notificationService, statisticsLogService);
	}
	
}
