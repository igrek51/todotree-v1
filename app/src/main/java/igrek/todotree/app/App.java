package igrek.todotree.app;

import android.support.v7.app.AppCompatActivity;

import igrek.todotree.controller.MainController;
import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.logger.Logs;

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
		Logs.info("Application started.");
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
