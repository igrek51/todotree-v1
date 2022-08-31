package igrek.todotree.ui.contextmenu

import android.app.Activity
import android.app.AlertDialog
import igrek.todotree.info.UiInfoService
import igrek.todotree.info.errorcheck.UiErrorHandler
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.intent.GUICommand
import igrek.todotree.intent.PersistenceCommand
import igrek.todotree.service.backup.Backup
import igrek.todotree.service.backup.BackupManager
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.service.tree.TreeScrollCache
import java.text.SimpleDateFormat
import java.util.*

class BackupListMenu(
    activity: LazyInject<Activity> = appFactory.activityMust,
    backupManager: LazyInject<BackupManager> = appFactory.backupManager,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    treeScrollCache: LazyInject<TreeScrollCache> = appFactory.treeScrollCache,
    treeManager: LazyInject<TreeManager> = appFactory.treeManager,
) {
    private val activity by LazyExtractor(activity)
    private val backupManager by LazyExtractor(backupManager)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val treeScrollCache by LazyExtractor(treeScrollCache)
    private val treeManager by LazyExtractor(treeManager)

    private val displayDateFormat: SimpleDateFormat =
        SimpleDateFormat("yyyy-MM-dd, HH:mm:ss", Locale.ENGLISH)

    fun show() {
        val actions = buildActionsList()
        val actionNames = convertToNamesArray(actions)
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Choose backup")
        builder.setItems(actionNames) { _, item ->
            try {
                actions[item].execute()
            } catch (t: Throwable) {
                UiErrorHandler.handleError(t)
            }
        }
        val alert = builder.create()
        alert.show()
    }

    private fun buildActionsList(): List<RestoreBackupAction> {
        val actions: MutableList<RestoreBackupAction> = ArrayList()
        val backups: List<Backup> = backupManager.getBackups()
        for (backup in backups) {
            actions.add(object : RestoreBackupAction(displayDateFormat.format(backup.date)) {
                override fun execute() {
                    PersistenceCommand().loadRootTreeFromBackup(backup)
                    uiInfoService.showInfo("Database backup loaded: " + backup.filename)
                }
            })
        }
        return actions
    }

    private fun convertToNamesArray(actions: List<RestoreBackupAction>): Array<CharSequence?> {
        val actionNames = arrayOfNulls<CharSequence>(actions.size)
        for (i in actions.indices) {
            actionNames[i] = actions[i].name
        }
        return actionNames
    }
}