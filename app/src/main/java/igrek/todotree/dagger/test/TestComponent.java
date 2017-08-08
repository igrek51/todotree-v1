package igrek.todotree.dagger.test;


import javax.inject.Singleton;

import dagger.Component;
import igrek.todotree.dagger.AppFactoryComponent;
import igrek.todotree.dagger.AppFactoryModule;

@Singleton
@Component(modules = {AppFactoryModule.class})
public interface TestComponent extends AppFactoryComponent {
	
	// Tests
	
	void inject(BaseDaggerTest there);
	
}