package igrek.todotree.ioc;


import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppFactoryModule.class})
public interface AppFactoryComponent {
	
	// allow to inject into our Main class
	// method name not important
	void inject(InjectedTest main);
}