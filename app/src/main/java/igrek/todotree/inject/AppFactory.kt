package igrek.todotree.inject

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import igrek.todotree.activity.*
import igrek.todotree.app.AppData
import igrek.todotree.info.UiInfoService
import igrek.todotree.info.logger.Logger
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.layout.LayoutController
import igrek.todotree.layout.navigation.NavigationMenuController
import igrek.todotree.layout.screen.HomeLayoutController
import igrek.todotree.persistence.LocalDataService
import igrek.todotree.persistence.user.UserDataDao
import igrek.todotree.service.access.AccessLogService
import igrek.todotree.service.access.DatabaseLock
import igrek.todotree.service.access.QuickAddService
import igrek.todotree.service.backup.BackupManager
import igrek.todotree.service.clipboard.SystemClipboardManager
import igrek.todotree.service.clipboard.TreeClipboardManager
import igrek.todotree.service.filesystem.FilesystemService
import igrek.todotree.service.history.ChangesHistory
import igrek.todotree.service.history.LinkHistoryService
import igrek.todotree.service.remote.RemoteDbRequester
import igrek.todotree.service.remote.RemotePushService
import igrek.todotree.service.statistics.StatisticsLogService
import igrek.todotree.service.system.SoftKeyboardService
import igrek.todotree.service.tree.*
import igrek.todotree.service.tree.persistence.TreePersistenceService
import igrek.todotree.settings.SettingsLayoutController
import igrek.todotree.settings.SettingsService
import igrek.todotree.settings.SettingsState
import igrek.todotree.system.SystemKeyDispatcher
import igrek.todotree.system.WindowManagerService
import igrek.todotree.ui.ExplosionService
import igrek.todotree.ui.GUI
import igrek.todotree.ui.edititem.EditItemLayout
import igrek.todotree.ui.treelist.TreeListLayout


class AppFactory(
    private var _activity: AppCompatActivity?,
) {

    val activity: LazyInject<Activity> = SingletonInject { _activity!! }
    val appCompatActivity: LazyInject<AppCompatActivity> = SingletonInject { _activity!! }

    val context: LazyInject<Context> = SingletonInject { _activity!!.applicationContext }
    val logger: LazyInject<Logger> = PrototypeInject { LoggerFactory.logger }

    /* Services */
    val activityData = SingletonInject { MainActivityData() }
    val activityController = SingletonInject { ActivityController() }
    val appInitializer = SingletonInject { AppInitializer() }
    val systemKeyDispatcher = SingletonInject { SystemKeyDispatcher() }
    val windowManagerService = SingletonInject { WindowManagerService() }
    val uiInfoService = SingletonInject { UiInfoService() }
    var settingsService = SingletonInject { SettingsService() }
    val layoutController = SingletonInject { LayoutController() }
    val softKeyboardService = SingletonInject { SoftKeyboardService() }
    val navigationMenuController = SingletonInject { NavigationMenuController() }
    val localDataService = SingletonInject { LocalDataService() }
    val settingsLayoutController = SingletonInject { SettingsLayoutController() }
    var settingsState = SingletonInject { SettingsState() }
    val activityResultDispatcher = SingletonInject { ActivityResultDispatcher() }
    val userDataDao = SingletonInject { UserDataDao() }
    val appData = SingletonInject { AppData() }
    val optionSelectDispatcher = SingletonInject { OptionSelectDispatcher() }
    val filesystemService = SingletonInject { FilesystemService() }
    var treeManager = SingletonInject { TreeManager() }
    val backupManager = SingletonInject { BackupManager() }
    var treePersistenceService = SingletonInject { TreePersistenceService() }
    val gui = SingletonInject { GUI() }
    var systemClipboardManager = SingletonInject { SystemClipboardManager() }
    var databaseLock = SingletonInject { DatabaseLock() }
    val accessLogService = SingletonInject { AccessLogService() }
    val changesHistory = SingletonInject { ChangesHistory() }
    val contentTrimmer = SingletonInject { ContentTrimmer() }
    val treeScrollCache = SingletonInject { TreeScrollCache() }
    val treeClipboardManager = SingletonInject { TreeClipboardManager() }
    val treeSelectionManager = SingletonInject { TreeSelectionManager() }
    val treeMover = SingletonInject { TreeMover() }
    val statisticsLogService = SingletonInject { StatisticsLogService() }
    val quickAddService = SingletonInject { QuickAddService() }
    val linkHistoryService = SingletonInject { LinkHistoryService() }
    val remoteDbRequester = SingletonInject { RemoteDbRequester() }
    val remotePushService = SingletonInject { RemotePushService() }
    val explosionService = SingletonInject { ExplosionService() }
    val homeLayoutController = SingletonInject { HomeLayoutController() }
    var treeListLayout = SingletonInject { TreeListLayout() }
    val editItemLayout = SingletonInject { EditItemLayout() }

    val inputMethodManager = PrototypeInject {
        appCompatActivity.get().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }
}
