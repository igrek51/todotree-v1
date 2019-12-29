package igrek.todotree.app;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import igrek.todotree.commands.GUICommand;
import igrek.todotree.commands.PersistenceCommand;
import igrek.todotree.logger.Logger;
import igrek.todotree.logger.LoggerFactory;

public class AppControllerService {
	
	public Activity activity;
	
	private boolean running = true;
	
	protected Logger logger = LoggerFactory.getLogger();
	
	public AppControllerService(Activity activity) {
		this.activity = activity;
	}
	
	public void init() {
		logger.info("Initializing application...");
		
		//hide task bar
		if (activity instanceof AppCompatActivity) {
			AppCompatActivity appCompatActivity = (AppCompatActivity) activity;
			if (appCompatActivity.getSupportActionBar() != null) {
				appCompatActivity.getSupportActionBar().hide();
			}
		}
		
		new GUICommand().guiInit();
		new PersistenceCommand().loadRootTree();
		new GUICommand().showItemsList();
		logger.info("Application has been initialized.");
	}
	
	public void quit() {
		if (!running) { //another attempt to close
			logger.warn("Closing app - ignoring another attempt");
			return;
		}
		logger.info("Closing app...");
		running = false;
		stopKeepingScreenOn();
		activity.finish();
	}
	
	public void stopKeepingScreenOn() {
		activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
	public void onResizeEvent(Configuration newConfig) {
		int screenWidthDp = newConfig.screenWidthDp;
		int screenHeightDp = newConfig.screenHeightDp;
		int orientation = newConfig.orientation;
		int densityDpi = newConfig.densityDpi;
		logger.debug("Screen size changed: " + screenWidthDp + "dp x " + screenHeightDp + "dp (DPI = " + densityDpi + ")");
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			logger.debug("Screen orientation changed: landscape");
		} else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			logger.debug("Screen orientation changed: portrait");
		}
	}
	
	public void minimize() {
		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		activity.startActivity(startMain);
	}
}
