package igrek.todotree.app;

import android.support.v7.app.AppCompatActivity;

import igrek.todotree.commands.GUICommand;
import igrek.todotree.commands.NavigationCommand;
import igrek.todotree.commands.PersistenceCommand;
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
		new GUICommand().guiInit();
		new PersistenceCommand().loadRootTree();
		new GUICommand().showItemsList();
		logger.info("Application has started.");
	}
	
	@Override
	public boolean optionsSelect(int id) {
		return new NavigationCommand().optionsSelect(id);
	}
	
	@Override
	public boolean onKeyBack() {
		return new NavigationCommand().backClicked();
	}
	
}
