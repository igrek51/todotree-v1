package igrek.todotree._dagger;


import org.junit.Test;

import igrek.todotree._dagger.base.BaseDaggerTest;

public class BaseDaggerTestTest extends BaseDaggerTest {
	
	@Test
	public void testActivityInjection() {
		System.out.println("injected activity: " + activity.toString());
	}
	
	@Test
	public void testLoggerMock() {
		logger.info("Hello dupa");
	}
	
}
