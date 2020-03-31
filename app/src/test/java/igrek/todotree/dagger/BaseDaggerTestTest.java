package igrek.todotree.dagger;


import org.junit.Test;

import igrek.todotree.dagger.base.BaseDaggerTest;

public class BaseDaggerTestTest extends BaseDaggerTest {
	
	@Test
	public void testActivityInjection() {
		System.out.println("injected activity: " + activity.toString());
	}
	
	@Test
	public void testLoggerMock() {
		this.getLogger().info("Hello dupa");
	}
	
}
