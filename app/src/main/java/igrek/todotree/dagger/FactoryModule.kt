package igrek.todotree.dagger


import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import dagger.Module
import dagger.Provides
import igrek.todotree.activity.ActivityController
import igrek.todotree.activity.AppInitializer
import igrek.todotree.activity.OptionSelectDispatcher
import igrek.todotree.app.AppData
import igrek.todotree.info.logger.Logger
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.service.access.AccessLogService
import igrek.todotree.service.access.DatabaseLock
import igrek.todotree.service.access.QuickAddService
import igrek.todotree.service.backup.BackupManager
import igrek.todotree.service.clipboard.SystemClipboardManager
import igrek.todotree.service.clipboard.TreeClipboardManager
import igrek.todotree.service.commander.SecretCommander
import igrek.todotree.service.filesystem.FilesystemService
import igrek.todotree.service.history.ChangesHistory
import igrek.todotree.service.history.LinkHistoryService
import igrek.todotree.service.preferences.Preferences
import igrek.todotree.service.resources.UserInfoService
import igrek.todotree.service.statistics.StatisticsLogService
import igrek.todotree.service.summary.AlarmService
import igrek.todotree.service.summary.DailySummaryService
import igrek.todotree.service.summary.NotificationService
import igrek.todotree.service.system.SoftKeyboardService
import igrek.todotree.service.tree.*
import igrek.todotree.service.tree.persistence.TreePersistenceService
import igrek.todotree.system.PackageInfoService
import igrek.todotree.system.PermissionService
import igrek.todotree.system.SystemKeyDispatcher
import igrek.todotree.system.WindowManagerService
import igrek.todotree.system.filesystem.ExternalCardService
import igrek.todotree.ui.GUI
import okhttp3.OkHttpClient
import javax.inject.Singleton

/**
 * Module with providers. These classes can be injected
 */
@Module
open class FactoryModule(private val activity: AppCompatActivity) {

    @Provides
    fun provideContext(): Context {
        return activity.applicationContext
    }

    @Provides
    fun provideActivity(): Activity {
        return activity
    }

    @Provides
    fun provideAppCompatActivity(): AppCompatActivity {
        return activity
    }

    @Provides
    open fun provideLogger(): Logger {
        return LoggerFactory.logger
    }

    /* Services */

    @Provides
    @Singleton
    fun aActivityController(): ActivityController = ActivityController()

    @Provides
    @Singleton
    fun aAppInitializer(): AppInitializer = AppInitializer()

    @Provides
    @Singleton
    fun aOptionSelectDispatcher(): OptionSelectDispatcher = OptionSelectDispatcher()

    @Provides
    @Singleton
    fun aSystemKeyDispatcher(): SystemKeyDispatcher = SystemKeyDispatcher()

    @Provides
    @Singleton
    fun aScreenService(): WindowManagerService = WindowManagerService()

    @Provides
    @Singleton
    fun aSoftKeyboardService(activity: Activity): SoftKeyboardService = SoftKeyboardService(activity)


    @Provides
    @Singleton
    fun aPermissionService(): PermissionService = PermissionService()

    @Provides
    @Singleton
    fun aPackageInfoService(): PackageInfoService = PackageInfoService()

    @Provides
    @Singleton
    fun aOkHttpClient(): OkHttpClient = OkHttpClient()

    @Provides
    @Singleton
    fun aFilesystemService(context: Context, externalCardService: ExternalCardService): FilesystemService = FilesystemService(context, externalCardService)

    @Provides
    @Singleton
    fun aTreeManager(changesHistory: ChangesHistory): TreeManager = TreeManager(changesHistory)

    @Provides
    @Singleton
    fun aPreferences(context: Context): Preferences = Preferences(context)

    @Provides
    @Singleton
    fun aBackupManager(preferences: Preferences, filesystem: FilesystemService): BackupManager = BackupManager(preferences, filesystem)

    @Provides
    @Singleton
    fun aUserInfoService(activity: Activity, gui: GUI): UserInfoService = UserInfoService(activity, gui)

    @Provides
    @Singleton
    fun aTreePersistenceService(): TreePersistenceService = TreePersistenceService()

    @Provides
    @Singleton
    fun aGUI(activity: AppCompatActivity): GUI = GUI(activity)

    @Provides
    @Singleton
    fun aSystemClipboardManager(activity: Activity): SystemClipboardManager = SystemClipboardManager(activity)

    @Provides
    @Singleton
    fun aAppData(): AppData = AppData()

    @Provides
    @Singleton
    fun aDatabaseLock(preferences: Preferences, accessLogService: AccessLogService): DatabaseLock = DatabaseLock(preferences, accessLogService)

    @Provides
    @Singleton
    fun aAccessLogService(filesystem: FilesystemService, preferences: Preferences): AccessLogService = AccessLogService(filesystem, preferences)

    @Provides
    @Singleton
    fun aChangesHistory(): ChangesHistory = ChangesHistory()

    @Provides
    @Singleton
    fun aContentTrimmer(): ContentTrimmer = ContentTrimmer()

    @Provides
    @Singleton
    fun aTreeScrollCache(): TreeScrollCache = TreeScrollCache()

    @Provides
    @Singleton
    fun aTreeClipboardManager(): TreeClipboardManager = TreeClipboardManager()

    @Provides
    @Singleton
    fun aTreeSelectionManager(): TreeSelectionManager = TreeSelectionManager()

    @Provides
    @Singleton
    fun aTreeMover(): TreeMover = TreeMover()

    @Provides
    @Singleton
    fun aSecretCommander(preferences: Preferences, userInfo: UserInfoService): SecretCommander = SecretCommander(preferences, userInfo)

    @Provides
    @Singleton
    fun aStatisticsLogService(filesystem: FilesystemService, preferences: Preferences): StatisticsLogService = StatisticsLogService(filesystem, preferences)

    @Provides
    @Singleton
    fun aExternalCardService(): ExternalCardService = ExternalCardService()

    @Provides
    @Singleton
    fun aQuickAddService(activity: Activity, treeManager: TreeManager, softKeyboardService: SoftKeyboardService, gui: dagger.Lazy<GUI>): QuickAddService = QuickAddService(activity, treeManager, softKeyboardService, gui)

    @Provides
    @Singleton
    fun aLinkHistoryService(): LinkHistoryService = LinkHistoryService()

    @Provides
    @Singleton
    fun aNotificationService(): NotificationService = NotificationService()

    @Provides
    @Singleton
    fun aAlarmService(activity: Activity): AlarmService = AlarmService(activity)

    @Provides
    @Singleton
    fun aDailySummaryService(activity: Activity, notificationService: NotificationService, statisticsLogService: StatisticsLogService): DailySummaryService = DailySummaryService(activity, notificationService, statisticsLogService)

    /*
	 * Empty service pattern:
	@Provides
    @Singleton
    fun a():  = ()

	 */

}
