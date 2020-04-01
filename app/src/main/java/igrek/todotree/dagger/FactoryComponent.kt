package igrek.todotree.dagger

import dagger.Component
import igrek.todotree.activity.*
import igrek.todotree.intent.*
import igrek.todotree.service.system.SoftKeyboardService
import igrek.todotree.system.PackageInfoService
import igrek.todotree.system.PermissionService
import igrek.todotree.system.SystemKeyDispatcher
import igrek.todotree.system.WindowManagerService
import igrek.todotree.ui.contextmenu.BackupListMenu
import igrek.todotree.ui.contextmenu.ItemActionsMenu
import igrek.todotree.ui.errorcheck.UIErrorHandler
import javax.inject.Singleton

/**
 * Dagger will be injecting to those classes
 */
@Singleton
@Component(modules = [FactoryModule::class])
interface FactoryComponent {

    fun inject(there: MainActivity)
    fun inject(there: QuickAddActivity)
    fun inject(there: UIErrorHandler)
    fun inject(there: ItemActionsMenu)
    fun inject(there: BackupListMenu)
    fun inject(there: NavigationCommand)
    fun inject(there: ExitCommand)
    fun inject(there: PersistenceCommand)
    fun inject(there: ClipboardCommand)
    fun inject(there: GUICommand)
    fun inject(there: TreeCommand)
    fun inject(there: ItemEditorCommand)
    fun inject(there: ItemTrashCommand)
    fun inject(there: ItemSelectionCommand)
    fun inject(there: ItemActionCommand)
    fun inject(there: StatisticsCommand)
    fun inject(there: WhatTheFuckCommand)

    fun inject(there: AppInitializer)
    fun inject(there: ActivityController)
    fun inject(there: WindowManagerService)
    fun inject(there: OptionSelectDispatcher)
    fun inject(there: SoftKeyboardService)
    fun inject(there: SystemKeyDispatcher)
    fun inject(there: PermissionService)
    fun inject(there: PackageInfoService)

}