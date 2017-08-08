package igrek.todotree.dagger;


import android.app.Activity;

import igrek.todotree.app.App;
import igrek.todotree.dagger.test.DaggerTestComponent;
import igrek.todotree.dagger.test.TestComponent;
import igrek.todotree.dagger.test.TestModule;

public class DaggerIOC {
	
	private static AppFactoryComponent appComponent;
	
	private DaggerIOC() {
	}
	
	public static void init(App app, Activity activity) {
		appComponent = DaggerAppFactoryComponent.builder()
				.appFactoryModule(new AppFactoryModule(app, activity))
				.build();
	}
	
	public static AppFactoryComponent getAppComponent() {
		return appComponent;
	}
	
	public static void initTest(App app, Activity activity) {
		appComponent = DaggerTestComponent.builder().testModule(new TestModule(app, activity))
				.build();
	}
	
	public static TestComponent getTestComponent() {
		return (TestComponent) appComponent;
	}
	
}
