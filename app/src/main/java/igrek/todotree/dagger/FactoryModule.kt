package igrek.todotree.dagger


import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import dagger.Lazy
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
import igrek.todotree.service.remote.RemoteDbRequester
import igrek.todotree.service.remote.RemotePushService
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
import igrek.todotree.ui.ExplosionService
import igrek.todotree.ui.GUI
import okhttp3.OkHttpClient
import javax.inject.Singleton

/**
 * Module with providers. These classes can be injected
 */
@Module
open class FactoryModule(private val activity: AppCompatActivity) {

    @Provides
    open fun provideContext(): Context {
        return activity.applicationContext
    }

    @Provides
    fun provideActivity(): Activity {
        return activity
    }

    @Provides
    open fun provideAppCompatActivity(): AppCompatActivity {
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
    open fun aFilesystemService(context: Context): FilesystemService = FilesystemService(context)

    @Provides
    @Singleton
    fun aTreeManager(changesHistory: ChangesHistory): TreeManager = TreeManager(changesHistory)

    @Provides
    @Singleton
    open fun aPreferences(context: Context): Preferences = Preferences(context)

    @Provides
    @Singleton
    fun aBackupManager(filesystem: FilesystemService): BackupManager = BackupManager(filesystem)

    @Provides
    @Singleton
    open fun aUserInfoService(activity: Activity, gui: GUI): UserInfoService = UserInfoService(activity, gui)

    @Provides
    @Singleton
    fun aTreePersistenceService(): TreePersistenceService = TreePersistenceService()

    @Provides
    @Singleton
    open fun aGUI(activity: AppCompatActivity): GUI = GUI(activity)

    @Provides
    @Singleton
    open fun aSystemClipboardManager(activity: Activity): SystemClipboardManager = SystemClipboardManager(activity)

    @Provides
    @Singleton
    fun aAppData(): AppData = AppData()

    @Provides
    @Singleton
    open fun aDatabaseLock(preferences: Preferences, accessLogService: AccessLogService): DatabaseLock = DatabaseLock(preferences, accessLogService)

    @Provides
    @Singleton
    fun aAccessLogService(filesystem: FilesystemService): AccessLogService = AccessLogService(filesystem)

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
    fun aStatisticsLogService(filesystem: FilesystemService): StatisticsLogService = StatisticsLogService(filesystem)

    @Provides
    @Singleton
    open fun aExternalCardService(): ExternalCardService = ExternalCardService()

    @Provides
    @Singleton
    fun aQuickAddService(activity: Activity, treeManager: TreeManager, gui: dagger.Lazy<GUI>, userInfoService: UserInfoService, activityController: ActivityController): QuickAddService = QuickAddService(activity, treeManager, gui, userInfoService, activityController)

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

    @Provides
    @Singleton
    fun aRemoteDbRequester(preferences: Preferences, activity: Activity): RemoteDbRequester = RemoteDbRequester(preferences, activity)

    @Provides
    @Singleton
    fun aRemotePushService(activity: Activity, treeManager: TreeManager, gui: Lazy<GUI>, userInfoService: UserInfoService, remoteDbRequester: RemoteDbRequester): RemotePushService = RemotePushService(activity, treeManager, gui, userInfoService, remoteDbRequester)

    @Provides
    @Singleton
    fun aExplosionService(activity: Activity): ExplosionService = ExplosionService(activity)

    /*
	 * Empty service pattern:
	@Provides
    @Singleton
    fun a():  = ()

	 */

}
