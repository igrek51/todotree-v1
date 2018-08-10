package igrek.todotree;

import android.app.Application;
import android.support.v7.app.AppCompatActivity;

import igrek.todotree.activity.CurrentActivityListener;
import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.logger.Logger;
import igrek.todotree.logger.LoggerFactory;

public class MainApplication extends Application {
	
	private Logger logger = LoggerFactory.getLogger();
	private CurrentActivityListener currentActivityListener = new CurrentActivityListener();
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		registerActivityLifecycleCallbacks(currentActivityListener);
		
		// Dagger Container init
		DaggerIOC.init(this);
		
		// catch all uncaught exceptions
		Thread.UncaughtExceptionHandler defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler((thread, th) -> {
			logger.errorUncaught(th);
			//pass further to OS
			defaultUEH.uncaughtException(thread, th);
		});
		
		logger.info("Application has been started");
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		unregisterActivityLifecycleCallbacks(currentActivityListener);
	}
	
	public AppCompatActivity getCurrentActivity() {
		return (AppCompatActivity) currentActivityListener.getCurrentActivity();
	}
}
