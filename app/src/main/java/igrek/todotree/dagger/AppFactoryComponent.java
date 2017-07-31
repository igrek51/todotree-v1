package igrek.todotree.dagger;


import javax.inject.Singleton;

import dagger.Component;
import igrek.todotree.app.App;
import igrek.todotree.gui.edititem.EditItemGUI;
import igrek.todotree.gui.numkeyboard.NumericKeyboardView;
import igrek.todotree.gui.treelist.TreeListView;
import igrek.todotree.controller.LogicActionController;

@Singleton
@Component(modules = {AppFactoryModule.class})
public interface AppFactoryComponent {
	
	void inject(IDaggerInjectionTest there);
	
	void inject(App there);
	
	void inject(TreeListView there);
	
	void inject(EditItemGUI there);
	
	void inject(NumericKeyboardView there);
	
	LogicActionController getActionController();
	
}