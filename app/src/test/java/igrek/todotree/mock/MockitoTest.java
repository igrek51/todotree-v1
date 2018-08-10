package igrek.todotree.mock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import igrek.todotree.MainApplication;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.when;

public class MockitoTest {
	
	private MainApplication application;
	
	@Before
	public void setUp() {
		application = Mockito.mock(MainApplication.class);
		
		when(application.toString()).thenReturn("dupa");
	}
	
	@Test
	public void testMocks() {
		System.out.println(application);
		assertThat(application.toString()).isEqualTo("dupa");
	}
	
}
