package igrek.todotree.ui.contextmenu

import android.app.AlertDialog
import igrek.todotree.service.backup.BackupManager
import java.text.SimpleDateFormat
import javax.inject.Inject

class BackupListMenu {
    @Inject
    var activity: Activity? = null

    @Inject
    var backupManager: BackupManager? = null

    @Inject
    var userInfo: UserInfoService? = null

    @Inject
    var scrollCache: TreeScrollCache? = null

    @Inject
    var treeManager: TreeManager? = null
    private val displayDateFormat: SimpleDateFormat =
        SimpleDateFormat("yyyy-MM-dd, HH:mm:ss", Locale.ENGLISH)

    fun show() {
        val actions = buildActionsList()
        val actionNames = convertToNamesArray(actions)
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Choose backup")
        builder.setItems(actionNames, object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, item: Int) {
                try {
                    actions[item].execute()
                } catch (t: Throwable) {
                    UIErrorHandler.showError(t)
                }
            }
        })
        val alert = builder.create()
        alert.show()
    }

    private fun buildActionsList(): List<RestoreBackupAction> {
        val actions: MutableList<RestoreBackupAction> = ArrayList()
        val backups: List<Backup> = backupManager!!.getBackups()
        for (backup in backups) {
            actions.add(object : RestoreBackupAction(displayDateFormat.format(backup.getDate())) {
                override fun execute() {
                    treeManager.reset()
                    scrollCache.clear()
                    PersistenceCommand().loadRootTreeFromBackup(backup)
                    GUICommand().updateItemsList()
                    userInfo.showInfo("Database backup loaded: " + backup.filename)
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

    init {
        DaggerIoc.factoryComponent.inject(this)
    }
}