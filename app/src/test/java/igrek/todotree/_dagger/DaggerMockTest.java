package igrek.todotree._dagger;


import org.junit.Test;

import igrek.todotree._dagger.base.BaseDaggerTest;

public class DaggerMockTest extends BaseDaggerTest {
	
	@Test
	public void testLoggerMock() {
		logger.info("Hello dupa");
	}
	
	@Test
	public void testMocks() {
		infoService.showInfo("DUPA");
	}
}