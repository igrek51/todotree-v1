package igrek.todotree.ioc;

import android.support.v7.app.AppCompatActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppFactoryModule {
	
	AppCompatActivity activity;
	
	public AppFactoryModule(AppCompatActivity activity) {
		this.activity = activity;
	}
	
	@Provides
	@Singleton
	AppCompatActivity provideActivity() {
		return activity;
	}
}
