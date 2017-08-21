package igrek.todotree.dagger;


import javax.inject.Singleton;

import dagger.Component;
import igrek.todotree.actions.ClipboardController;
import igrek.todotree.actions.ExitController;
import igrek.todotree.actions.GUIController;
import igrek.todotree.actions.ItemActionController;
import igrek.todotree.actions.ItemEditorController;
import igrek.todotree.actions.ItemSelectionController;
import igrek.todotree.actions.ItemTrashController;
import igrek.todotree.actions.MainController;
import igrek.todotree.actions.PersistenceController;
import igrek.todotree.actions.TreeController;
import igrek.todotree.ui.contextmenu.BackupListMenu;
import igrek.todotree.ui.contextmenu.ItemActionsMenu;
import igrek.todotree.ui.errorcheck.UIErrorHandler;

@Singleton
@Component(modules = {AppFactoryModule.class})
public interface AppFactoryComponent {
	
	void inject(UIErrorHandler there);
	
	void inject(ItemActionsMenu there);
	
	void inject(BackupListMenu there);
	
	// Controllers
	
	void inject(MainController there);
	
	void inject(ExitController there);
	
	void inject(PersistenceController there);
	
	void inject(ClipboardController there);
	
	void inject(GUIController there);
	
	void inject(TreeController there);
	
	void inject(ItemEditorController there);
	
	void inject(ItemTrashController there);
	
	void inject(ItemSelectionController there);
	
	void inject(ItemActionController there);
	
}