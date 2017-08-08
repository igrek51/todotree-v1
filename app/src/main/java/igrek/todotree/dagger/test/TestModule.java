package igrek.todotree.dagger.test;

import android.app.Activity;

import dagger.Module;
import igrek.todotree.app.App;
import igrek.todotree.dagger.AppFactoryModule;

@Module
public class TestModule extends AppFactoryModule {
	
	
	public TestModule(App app, Activity activity) {
		super(app, activity);
	}
	
	
}
