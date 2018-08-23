package igrek.todotree.dagger;

import android.app.Activity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import javax.inject.Inject;

import igrek.todotree.BuildConfig;
import igrek.todotree.MainApplication;
import igrek.todotree.dagger.base.DaggerTestComponent;
import igrek.todotree.dagger.base.TestComponent;
import igrek.todotree.dagger.base.TestModule;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, application = MainApplication.class)
public class SimpleDaggerInjectionTest {
	
	@Inject
	Activity activity;
	
	@Before
	public void setUp() {
		MainApplication application = (MainApplication) RuntimeEnvironment.application;
		
		TestComponent component = DaggerTestComponent.builder()
				.factoryModule(new TestModule(application))
				.build();
		
		DaggerIOC.setFactoryComponent(component);
		
		component.inject(this);
	}
	
	@Test
	public void testActivityInjection() {
		System.out.println("injected activity: " + activity.toString());
	}
	
}
