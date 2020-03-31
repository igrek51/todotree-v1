package igrek.todotree.dagger;


import org.junit.Test;

import igrek.todotree.dagger.base.BaseDaggerTest;

public class DaggerMockTest extends BaseDaggerTest {
	
	@Test
	public void testLoggerMock() {
		getLogger().info("Hello dupa");
	}
	
	@Test
	public void testMocks() {
		infoService.showInfo("DUPA");
	}
}