package igrek.todotree.app;

import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import igrek.todotree.controller.LogicActionController;
import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.gui.GUI;
import igrek.todotree.logger.Logs;
import igrek.todotree.services.datatree.TreeManager;

//TODO brak zapisu bazy jeśli nie było zmian

//TODO: funkcja cofania zmian - zapisywanie modyfikacji, dodawania, usuwania elementów, przesuwania

//TODO SERWISY: do blokowania bazy, do pobierania treści komunikatów, stan aplikacji

public class App extends BaseApp {
	
	@Inject
	TreeManager treeManager;
	
	@Inject
	GUI gui;
	
	@Inject
	LogicActionController actionController;
	
	public App(AppCompatActivity activity) {
		super(activity);
		// Dagger init
		DaggerIOC.init(this, activity);
		DaggerIOC.getAppComponent().inject(this);
	}
	
	@Override
	public void init() {
		super.init();
		
		treeManager.loadRootTree();
		
		gui.lazyInit();
		gui.showItemsList(treeManager.getCurrentItem());
		
		Logs.info("Application started.");
	}
	
	@Override
	public boolean optionsSelect(int id) {
		return actionController.optionsSelect(id);
	}
	
	@Override
	public boolean onKeyBack() {
		actionController.backClicked();
		return true;
	}
	
}
