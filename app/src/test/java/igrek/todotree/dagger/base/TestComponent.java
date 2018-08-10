package igrek.todotree.dagger.base;

import javax.inject.Singleton;

import dagger.Component;
import igrek.todotree.dagger.FactoryComponent;
import igrek.todotree.dagger.FactoryModule;
import igrek.todotree.dagger.SimpleDaggerInjectionTest;
import igrek.todotree.service.tree.TreeManagerTest;

@Singleton
@Component(modules = {FactoryModule.class})
public interface TestComponent extends FactoryComponent {
	
	// to use dagger injection in tests
	void inject(SimpleDaggerInjectionTest there);
	
	void inject(BaseDaggerTest there);
	
	void inject(TreeManagerTest there);
	
}