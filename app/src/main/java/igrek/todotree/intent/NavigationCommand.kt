package igrek.todotree.intent

import android.content.Context
import igrek.todotree.R
import igrek.todotree.activity.ActivityController
import igrek.todotree.app.AppData
import igrek.todotree.app.AppState
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.command.Commander
import igrek.todotree.service.tree.TreeSelectionManager
import igrek.todotree.ui.GUI

class NavigationCommand(
    context: LazyInject<Context> = appFactory.context,
    gui: LazyInject<GUI> = appFactory.gui,
    appData: LazyInject<AppData> = appFactory.appData,
    activityController: LazyInject<ActivityController> = appFactory.activityController,
    treeSelectionManager: LazyInject<TreeSelectionManager> = appFactory.treeSelectionManager,
) {
    private val context by LazyExtractor(context)
    private val gui by LazyExtractor(gui)
    private val appData by LazyExtractor(appData)
    private val activityController by LazyExtractor(activityController)
    private val treeSelectionManager by LazyExtractor(treeSelectionManager)

    fun optionsSelect(id: Int): Boolean {
        when (id) {
            R.id.action_minimize -> {
                activityController.minimize()
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
            R.id.action_enter_command -> {
                Commander(context).showCommandAlert()
                return false
            }
        }
        return false
    }

    fun backClicked(): Boolean {
        if (appData.isState(AppState.ITEMS_LIST)) {
            if (treeSelectionManager.isAnythingSelected) {
                treeSelectionManager.cancelSelectionMode()
                GUICommand().updateItemsList()
            } else {
                TreeCommand().goBack()
            }
        } else if (appData.isState(AppState.EDIT_ITEM_CONTENT)) {
            if (gui.editItemBackClicked()) return true
            ItemEditorCommand().cancelEditedItem()
        }
        return true
    }

    fun approveClicked(): Boolean {
        if (appData.isState(AppState.EDIT_ITEM_CONTENT)) {
            gui.requestSaveEditedItem()
        } else if (appData.isState(AppState.ITEMS_LIST)) {
            if (treeSelectionManager.isAnythingSelected) {
                treeSelectionManager.cancelSelectionMode()
                GUICommand().updateItemsList()
            } else {
                TreeCommand().goBack()
            }
        }
        return true
    }
}