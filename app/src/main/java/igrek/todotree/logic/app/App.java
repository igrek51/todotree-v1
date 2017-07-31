package igrek.todotree.logic.app;

import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import javax.inject.Inject;

import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.gui.GUI;
import igrek.todotree.logger.Logs;
import igrek.todotree.logic.LogicActionController;
import igrek.todotree.logic.datatree.TreeManager;
import igrek.todotree.preferences.Preferences;

//TODO brak zapisu bazy jeśli nie było zmian

//TODO: funkcja cofania zmian - zapisywanie modyfikacji, dodawania, usuwania elementów, przesuwania

//TODO SERWISY: do blokowania bazy, do pobierania treści komunikatów, stan aplikacji

public class App extends BaseApp {
	
	@Inject
	TreeManager treeManager;
	
	@Inject
	Preferences preferences;
	
	@Inject
	GUI gui;
	
	@Inject
	LogicActionController actionController;
	
	private AppState state;
	
	public App(AppCompatActivity activity) {
		super(activity);
		// Dagger init
		DaggerIOC.init(this, activity);
		DaggerIOC.getAppComponent().inject(this);
	}
	
	@Override
	public void init() {
		super.init();
		
		gui.lazyInit();
		
		treeManager.loadRootTree();
		
		gui.showItemsList(treeManager.getCurrentItem());
		
		state = AppState.ITEMS_LIST;
		
		Logs.info("Application started.");
	}
	
	public AppState getState() {
		return state;
	}
	
	public void setState(AppState state) {
		this.state = state;
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
	
	@Override
	public void menuInit(Menu menu) {
		super.menuInit(menu);
		
		//TODO: zmiana widoczności opcji menu przy zaznaczaniu wielu elementów i kopiowaniu (niepusty schowek, niepuste zaznaczenie)
		//TODO: zmiana widoczności opcji menu przy edycji elementu
		
		//setMenuItemVisible(R.id.action_copy, false);
		//item.setTitle(title);
		//item.setIcon(iconRes); //int iconRes
	}
	
}
