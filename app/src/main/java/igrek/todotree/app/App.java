package igrek.todotree.app;

import android.support.v7.app.AppCompatActivity;

import igrek.todotree.actions.GUIController;
import igrek.todotree.actions.MainController;
import igrek.todotree.actions.PersistenceController;
import igrek.todotree.dagger.DaggerIOC;

//TODO item types: link with name, checkbox, text with date / hour, text with number, separator, separator with group name
//TODO show changes: transaction commit, rollback, revert last change

public class App extends BaseApp {
	
	public App(AppCompatActivity activity) {
		super(activity);
		// Dagger init
		DaggerIOC.init(this, activity);
	}
	
	@Override
	public void init() {
		super.init();
		new GUIController().guiInit();
		new PersistenceController().loadRootTree();
		new GUIController().showItemsList();
		logger.info("Application has started.");
	}
	
	@Override
	public boolean optionsSelect(int id) {
		return new MainController().optionsSelect(id);
	}
	
	@Override
	public boolean onKeyBack() {
		return new MainController().backClicked();
	}
	
}
