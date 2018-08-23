package igrek.todotree.dagger;


import android.support.v7.app.AppCompatActivity;

public class DaggerIOC {
	
	private static FactoryComponent appComponent;
	
	private DaggerIOC() {
	}
	
	public static void init(AppCompatActivity activity) {
		appComponent = DaggerFactoryComponent.builder().factoryModule(new FactoryModule(activity))
				.build();
	}
	
	public static FactoryComponent getFactoryComponent() {
		return appComponent;
	}
	
	/**
	 * only for testing purposes
	 */
	public static void setFactoryComponent(FactoryComponent component) {
		appComponent = component;
	}
	
}
