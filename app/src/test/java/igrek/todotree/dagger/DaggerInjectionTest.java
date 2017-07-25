package igrek.todotree.dagger;


import android.app.Activity;

import javax.inject.Inject;

import igrek.todotree.logger.Logs;

public class DaggerInjectionTest implements IDaggerInjectionTest {
	
	@Inject
	Activity activity;
	
	public DaggerInjectionTest() {
		
		DaggerIOC.getAppComponent().inject(this);
		
	}
	
	public void test() {
		Logs.info("injected activity: " + activity.toString());
	}
}
