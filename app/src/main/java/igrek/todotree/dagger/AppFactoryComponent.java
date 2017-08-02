package igrek.todotree.dagger;


import javax.inject.Singleton;

import dagger.Component;
import igrek.todotree.app.App;
import igrek.todotree.controller.ExitController;
import igrek.todotree.controller.MainController;
import igrek.todotree.controller.PersistenceController;
import igrek.todotree.gui.edititem.EditItemGUI;
import igrek.todotree.gui.numkeyboard.NumericKeyboardView;
import igrek.todotree.gui.treelist.TreeListView;

@Singleton
@Component(modules = {AppFactoryModule.class})
public interface AppFactoryComponent {
	
	void inject(IDaggerInjectionTest there);
	
	void inject(App there);
	
	void inject(TreeListView there);
	
	void inject(EditItemGUI there);
	
	void inject(NumericKeyboardView there);
	
	// Controllers
	
	void inject(MainController there);
	
	void inject(ExitController there);
	
	void inject(PersistenceController there);
	
}