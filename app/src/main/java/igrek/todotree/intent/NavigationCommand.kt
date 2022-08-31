package igrek.todotree.intent

import igrek.todotree.dagger.FactoryComponent.inject
import igrek.todotree.activity.ActivityController.minimize
import igrek.todotree.intent.ExitCommand.exitApp
import igrek.todotree.intent.ExitCommand.optionSaveAndExit
import igrek.todotree.intent.PersistenceCommand.optionSave
import igrek.todotree.intent.PersistenceCommand.optionReload
import igrek.todotree.intent.PersistenceCommand.optionRestoreBackup
import igrek.todotree.intent.ItemSelectionCommand.toggleSelectAll
import igrek.todotree.intent.ClipboardCommand.cutSelectedItems
import igrek.todotree.intent.ClipboardCommand.copySelectedItems
import igrek.todotree.intent.ItemSelectionCommand.sumItems
import igrek.todotree.intent.TreeCommand.goUp
import igrek.todotree.remote.RemoteCommander.showCommandAlert
import igrek.todotree.app.AppData.isState
import igrek.todotree.intent.GUICommand.updateItemsList
import igrek.todotree.intent.TreeCommand.goBack
import igrek.todotree.ui.GUI.editItemBackClicked
import igrek.todotree.intent.ItemEditorCommand.cancelEditedItem
import igrek.todotree.ui.GUI.requestSaveEditedItem
import javax.inject.Inject
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.ui.GUI
import igrek.todotree.activity.ActivityController
import igrek.todotree.app.AppData
import igrek.todotree.service.tree.TreeSelectionManager
import igrek.todotree.service.summary.NotificationService
import igrek.todotree.service.summary.AlarmService
import igrek.todotree.R
import igrek.todotree.intent.ExitCommand
import igrek.todotree.intent.PersistenceCommand
import igrek.todotree.intent.ItemSelectionCommand
import igrek.todotree.intent.ClipboardCommand
import igrek.todotree.intent.StatisticsCommand
import igrek.todotree.intent.TreeCommand
import igrek.todotree.remote.RemoteCommander
import org.joda.time.DateTime
import igrek.todotree.app.AppState
import igrek.todotree.intent.GUICommand
import igrek.todotree.intent.ItemEditorCommand
import igrek.todotree.dagger.DaggerIoc

class NavigationCommand {
    @JvmField
	@Inject
    var treeManager: TreeManager? = null

    @JvmField
	@Inject
    var gui: GUI? = null

    @JvmField
	@Inject
    var activityController: ActivityController? = null

    @JvmField
	@Inject
    var appData: AppData? = null

    @JvmField
	@Inject
    var selectionManager: TreeSelectionManager? = null

    @JvmField
	@Inject
    var notificationService: NotificationService? = null

    @JvmField
	@Inject
    var alarmService: AlarmService? = null
    fun optionsSelect(id: Int): Boolean {
        when (id) {
            R.id.action_minimize -> {
                activityController!!.minimize()
                return true
            }
            R.id.action_exit_without_saving -> {
                ExitCommand().exitApp()
                return true
            }
            R.id.action_save_exit -> {
                ExitCommand().optionSaveAndExit()
                return true
            }
            R.id.action_save -> {
                PersistenceCommand().optionSave()
                return true
            }
            R.id.action_reload -> {
                PersistenceCommand().optionReload()
                return true
            }
            R.id.action_restore_backup -> {
                PersistenceCommand().optionRestoreBackup()
                return true
            }
            R.id.action_select_all -> {
                ItemSelectionCommand().toggleSelectAll()
                return false
            }
            R.id.action_cut -> {
                ClipboardCommand().cutSelectedItems()
                return false
            }
            R.id.action_copy -> {
                ClipboardCommand().copySelectedItems()
                return false
            }
            R.id.action_sum_selected -> {
                ItemSelectionCommand().sumItems()
                return false
            }
            R.id.action_show_statistics -> {
                StatisticsCommand().showStatisticsInfo()
                return false
            }
            R.id.action_go_up -> {
                TreeCommand().goUp()
                return false
            }
            R.id.action_notify -> {
                summaryNotify()
                return false
            }
            R.id.action_enter_command -> {
                RemoteCommander(activityController!!.activity.get()).showCommandAlert()
                return false
            }
        }
        return false
    }

    private fun summaryNotify() {
        alarmService!!.setAlarmAt(DateTime.now().plusSeconds(10))
    }

    fun backClicked(): Boolean {
        if (appData!!.isState(AppState.ITEMS_LIST)) {
            if (selectionManager!!.isAnythingSelected) {
                selectionManager!!.cancelSelectionMode()
                GUICommand().updateItemsList()
            } else {
                TreeCommand().goBack()
            }
        } else if (appData!!.isState(AppState.EDIT_ITEM_CONTENT)) {
            if (gui!!.editItemBackClicked()) return true
            ItemEditorCommand().cancelEditedItem()
        }
        return true
    }

    fun approveClicked(): Boolean {
        if (appData!!.isState(AppState.EDIT_ITEM_CONTENT)) {
            gui!!.requestSaveEditedItem()
        } else if (appData!!.isState(AppState.ITEMS_LIST)) {
            if (selectionManager!!.isAnythingSelected) {
                selectionManager!!.cancelSelectionMode()
                GUICommand().updateItemsList()
            } else {
                TreeCommand().goBack()
            }
        }
        return true
    }

    init {
        DaggerIoc.factoryComponent.inject(this)
    }
}