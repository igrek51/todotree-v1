package igrek.todotree.ioc;


import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import igrek.todotree.logger.Logs;

public class InjectedTest {
	
	@Inject
	AppCompatActivity activity;
	
	private AppFactoryComponent component;
	
	public InjectedTest() {
		component = DaggerAppFactoryComponent.builder().build();
		component.inject(this);
	}
	
	public void test() {
		Logs.info(activity.toString());
	}
}
