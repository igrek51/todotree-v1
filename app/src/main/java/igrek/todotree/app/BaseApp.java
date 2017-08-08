package igrek.todotree.app;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import igrek.todotree.logger.Logs;

public abstract class BaseApp {
	
	private static final int FULLSCREEN_FLAG = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
	private static final boolean FULLSCREEN = false;
	private static final boolean HIDE_TASKBAR = true;
	private static final boolean KEEP_SCREEN_ON = false;
	
	public AppCompatActivity activity;
	private Thread.UncaughtExceptionHandler defaultUEH;
	
	private boolean running = true;
	
	protected Logs logger = new Logs();
	
	BaseApp(AppCompatActivity activity) {
		this.activity = activity;
	}
	
	public void init() {
		logger.info("Initializing application...");
		
		// catch all uncaught exceptions
		defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable th) {
				logger.errorUncaught(th);
				//pass further to OS
				defaultUEH.uncaughtException(thread, th);
			}
		});
		
		//hide task bar
		if (HIDE_TASKBAR) {
			if (activity.getSupportActionBar() != null) {
				activity.getSupportActionBar().hide();
			}
		}
		//fullscreen
		if (FULLSCREEN) {
			activity.getWindow().setFlags(FULLSCREEN_FLAG, FULLSCREEN_FLAG);
		}
		if (KEEP_SCREEN_ON) {
			activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}
	
	public void pause() {
	}
	
	public void resume() {
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
	
	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
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
	
	public boolean onKeyBack() {
		quit();
		return true;
	}
	
	public boolean onKeyMenu() {
		return false;
	}
	
	public boolean optionsSelect(int id) {
		return false;
	}
	
	public void minimize() {
		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		activity.startActivity(startMain);
	}
}
