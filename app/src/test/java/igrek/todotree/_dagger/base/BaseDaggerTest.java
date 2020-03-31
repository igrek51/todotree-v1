package igrek.todotree._dagger.base;

import android.app.Activity;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import javax.inject.Inject;

import igrek.todotree.BuildConfig;
import igrek.todotree.MainApplication;
import igrek.todotree.logger.Logger;
import igrek.todotree.service.access.DatabaseLock;
import igrek.todotree.service.clipboard.TreeClipboardManager;
import igrek.todotree.service.resources.UserInfoService;
import igrek.todotree.service.tree.TreeManager;
import igrek.todotree.service.tree.TreeScrollCache;
import igrek.todotree.service.tree.persistence.TreePersistenceService;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, application = MainApplication.class, manifest = "src/main/AndroidManifest.xml", packageName = "igrek.todotree")
public abstract class BaseDaggerTest {
	
	@Inject
	protected Activity activity;
	
	@Inject
	protected Logger logger;
	
	
	@Inject
	protected TreeManager treeManager;
	
	@Inject
	protected TreeClipboardManager treeClipboardManager;
	
	@Inject
	protected TreeScrollCache scrollCache;
	
	@Inject
	protected TreePersistenceService persistenceService;
	
	@Inject
	protected UserInfoService infoService;
	
	@Inject
	protected DatabaseLock dbLock;
	
	
	@Before
	public void setUp() {
		MainApplication application = (MainApplication) RuntimeEnvironment.application;
		
		TestComponent component = DaggerTestComponent.builder()
				.factoryModule(new TestModule(application))
				.build();
		
		DaggerIoc.setFactoryComponent(component);
		
		injectThis(component);
	}
	
	protected void injectThis(TestComponent component) {
		component.inject(this);
	}
}
