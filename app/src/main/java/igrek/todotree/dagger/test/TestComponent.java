package igrek.todotree.dagger.test;


import javax.inject.Singleton;

import dagger.Component;
import igrek.todotree.dagger.FactoryComponent;
import igrek.todotree.dagger.FactoryModule;

@Singleton
@Component(modules = {FactoryModule.class})
public interface TestComponent extends FactoryComponent {
	
	// Tests
	void inject(BaseDaggerTest there);
	
}