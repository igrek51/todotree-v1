package igrek.todotree.app;

import android.support.v7.app.AppCompatActivity;

import igrek.todotree.controller.GUIController;
import igrek.todotree.controller.MainController;
import igrek.todotree.controller.PersistenceController;
import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.logger.Logs;

//TODO paste as link: >, detecting broken links
//TODO option restore selected backup
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
		Logs.info("Started application.");
	}
	
	@Override
	public boolean optionsSelect(int id) {
		return new MainController().optionsSelect(id);
	}
	
	@Override
	public boolean onKeyBack() {
		new MainController().backClicked();
		return true;
	}
	
}
