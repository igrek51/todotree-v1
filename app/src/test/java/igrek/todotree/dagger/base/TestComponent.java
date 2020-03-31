package igrek.todotree.dagger.base;

import javax.inject.Singleton;

import dagger.Component;
import igrek.todotree.dagger.FactoryComponent;
import igrek.todotree.dagger.FactoryModule;
import igrek.todotree.service.tree.TreeManagerTest;

@Singleton
@Component(modules = {FactoryModule.class})
public interface TestComponent extends FactoryComponent {
	
	void inject(BaseDaggerTest there);
	
	void inject(TreeManagerTest there);
	
}