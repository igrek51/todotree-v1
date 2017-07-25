package igrek.todotree.dagger;


import android.app.Activity;

public class DaggerIOC {
	
	private static AppFactoryComponent appComponent;
	
	private DaggerIOC() {}
	
	public static AppFactoryComponent getAppComponent() {
		return appComponent;
	}
	
	public static void init(Activity activity) {
		appComponent = initDagger(activity);
	}
	
	private static AppFactoryComponent initDagger(Activity activity) {
		return DaggerAppFactoryComponent.builder()
				.appFactoryModule(new AppFactoryModule(activity))
				.build();
	}
}
