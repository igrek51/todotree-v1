package igrek.todotree.activity;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class CurrentActivityListener implements Application.ActivityLifecycleCallbacks {
	
	private Activity currentActivity;
	
	public AppCompatActivity getCurrentActivity() {
		return (AppCompatActivity) currentActivity;
	}
	
	@Override
	public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
		currentActivity = activity;
	}
	
	@Override
	public void onActivityStarted(Activity activity) {
		currentActivity = activity;
	}
	
	@Override
	public void onActivityResumed(Activity activity) {
		currentActivity = activity;
	}
	
	@Override
	public void onActivityPaused(Activity activity) {
	
	}
	
	@Override
	public void onActivityStopped(Activity activity) {
	
	}
	
	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
	
	}
	
	@Override
	public void onActivityDestroyed(Activity activity) {
	
	}
}
