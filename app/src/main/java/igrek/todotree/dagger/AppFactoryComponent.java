package igrek.todotree.dagger;


import javax.inject.Singleton;

import dagger.Component;
import igrek.todotree.controller.ClipboardController;
import igrek.todotree.controller.ExitController;
import igrek.todotree.controller.GUIController;
import igrek.todotree.controller.ItemActionController;
import igrek.todotree.controller.ItemEditorController;
import igrek.todotree.controller.ItemSelectionController;
import igrek.todotree.controller.ItemTrashController;
import igrek.todotree.controller.MainController;
import igrek.todotree.controller.PersistenceController;
import igrek.todotree.controller.TreeController;
import igrek.todotree.ui.contextmenu.ItemActionsMenu;
import igrek.todotree.ui.errorcheck.UIErrorHandler;

@Singleton
@Component(modules = {AppFactoryModule.class})
public interface AppFactoryComponent {
	
	void inject(UIErrorHandler there);
	
	void inject(ItemActionsMenu there);
	
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