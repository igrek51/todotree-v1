package igrek.todotree.dagger.test;


import javax.inject.Singleton;

import dagger.Component;
import igrek.todotree.dagger.AppFactoryComponent;

@Singleton
@Component(modules = {TestModule.class})
public interface TestComponent extends AppFactoryComponent {
	
	// Tests
	
	void inject(BaseDaggerTest there);
	
}