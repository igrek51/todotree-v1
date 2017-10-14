package igrek.todotree.dagger;


import org.junit.Before;
import org.junit.Test;

import igrek.todotree.MainActivity;
import igrek.todotree.dagger.test.BaseDaggerTest;

import static org.mockito.Mockito.mock;

public class DaggerInjectionTest extends BaseDaggerTest {
	
	@Before
	public void setUp() throws Exception {
		MainActivity activity = mock(MainActivity.class);
		// Dagger init test
		DaggerIOC.initTest(null, activity);
		DaggerIOC.getTestComponent().inject(this);
	}
	
	@Test
	public void testInjections() {
		System.out.println("injected activity: " + activity.toString());
	}
	
}
