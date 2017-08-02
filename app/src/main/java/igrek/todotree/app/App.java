package igrek.todotree.app;

import android.support.v7.app.AppCompatActivity;

import igrek.todotree.controller.MainController;
import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.logger.Logs;

//TODO paste as link: >, detecting broken links
//TODO context menu on item long click or nearby button: select, add above, paste above, remove, paste above as links
//TODO show changes: transaction commit, rollback, revert last change
//TODO json serializing
//TODO item types: link with name, checkbox, text with date / hour, text with number, separator, separator with group name

public class App extends BaseApp {
	
	public App(AppCompatActivity activity) {
		super(activity);
		// Dagger init
		DaggerIOC.init(this, activity);
	}
	
	@Override
	public void init() {
		super.init();
		new MainController().initializeApp();
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
