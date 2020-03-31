package igrek.todotree._dagger.base;

import javax.inject.Singleton;

import dagger.Component;
import igrek.todotree._dagger.FactoryModule;
import igrek.todotree._dagger.SimpleDaggerInjectionTest;
import igrek.todotree.service.tree.TreeManagerTest;

@Singleton
@Component(modules = {FactoryModule.class})
public interface TestComponent extends FactoryComponent {
	
	// to use dagger injection in tests
	void inject(SimpleDaggerInjectionTest there);
	
	void inject(BaseDaggerTest there);
	
	void inject(TreeManagerTest there);
	
}