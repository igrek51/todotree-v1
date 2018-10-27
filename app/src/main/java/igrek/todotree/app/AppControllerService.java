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
import igrek.todotree.service.summary.DailySummaryService;

//TODO item types: link with name, checkbox, text with date / hour, text with number, separator, separator with group name
//TODO show changes: transaction commit, rollback, revert last change

public class AppControllerService {
	
	private static final int FULLSCREEN_FLAG = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
	private static final boolean FULLSCREEN = false;
	private static final boolean HIDE_TASKBAR = true;
	private static final boolean KEEP_SCREEN_ON = false;
	
	public Activity activity;
	private DailySummaryService dailySummaryService;
	
	private boolean running = true;
	
	protected Logger logger = LoggerFactory.getLogger();
	
	public AppControllerService(Activity activity, DailySummaryService dailySummaryService) {
		this.activity = activity;
		this.dailySummaryService = dailySummaryService;
	}
	
	public void init() {
		logger.info("Initializing application...");
		
		//hide task bar
		if (HIDE_TASKBAR) {
			if (activity instanceof AppCompatActivity) {
				AppCompatActivity appCompatActivity = (AppCompatActivity) activity;
				if (appCompatActivity.getSupportActionBar() != null) {
					appCompatActivity.getSupportActionBar().hide();
				}
			}
		}
		//fullscreen
		if (FULLSCREEN) {
			activity.getWindow().setFlags(FULLSCREEN_FLAG, FULLSCREEN_FLAG);
		}
		if (KEEP_SCREEN_ON) {
			activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		
		new GUICommand().guiInit();
		new PersistenceCommand().loadRootTree();
		new GUICommand().showItemsList();
		logger.info("Application has been initialized.");
	}
	
	public void runExtraAction(String action) {
		logger.debug("Running extra action " + action);
		switch (action) {
			case DailySummaryService.DAILY_SUMMARY_ACTION:
				activity.moveTaskToBack(true);
				dailySummaryService.showSummaryNotification();
				break;
			default:
				logger.warn("Unknown action: " + action);
		}
	}
	
	public void quit() {
		if (!running) { //another attempt to close
			logger.warn("Closing app - ignoring another attempt");
			return;
		}
		logger.info("Closing app...");
		running = false;
		activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		activity.finish();
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
