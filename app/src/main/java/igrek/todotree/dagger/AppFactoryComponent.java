package igrek.todotree.dagger;


import javax.inject.Singleton;

import dagger.Component;
import igrek.todotree.commands.ClipboardCommand;
import igrek.todotree.commands.ExitCommand;
import igrek.todotree.commands.GUICommand;
import igrek.todotree.commands.ItemActionCommand;
import igrek.todotree.commands.ItemEditorCommand;
import igrek.todotree.commands.ItemSelectionCommand;
import igrek.todotree.commands.ItemTrashCommand;
import igrek.todotree.commands.NavigationCommand;
import igrek.todotree.commands.PersistenceCommand;
import igrek.todotree.commands.TreeCommand;
import igrek.todotree.ui.contextmenu.BackupListMenu;
import igrek.todotree.ui.contextmenu.ItemActionsMenu;
import igrek.todotree.ui.errorcheck.UIErrorHandler;

/**
 * Dagger will be injecting to those classes
 */
@Singleton
@Component(modules = {AppFactoryModule.class})
public interface AppFactoryComponent {
	
	void inject(UIErrorHandler there);
	
	void inject(ItemActionsMenu there);
	
	void inject(BackupListMenu there);
	
	// Controllers
	
	void inject(NavigationCommand there);
	
	void inject(ExitCommand there);
	
	void inject(PersistenceCommand there);
	
	void inject(ClipboardCommand there);
	
	void inject(GUICommand there);
	
	void inject(TreeCommand there);
	
	void inject(ItemEditorCommand there);
	
	void inject(ItemTrashCommand there);
	
	void inject(ItemSelectionCommand there);
	
	void inject(ItemActionCommand there);
	
}