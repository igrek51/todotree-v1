package igrek.todotree.services.filesystem;

import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class FirstFinderTest {
	
	@Test
	public void testEmptyRules() {
		assertThat(new FirstFinder().find()).isNull();
		assertThat(new FirstFinder().addRule(null).find()).isNull();
	}
	
	@Test
	public void testDefaultValue() {
		assertThat(new FirstFinder<String>().addRule("Dupa").find()).isEqualTo("Dupa");
		assertThat(new FirstFinder().addRule(51).find()).isEqualTo(51);
		assertThat(new FirstFinder<Number>().addRule(null).find()).isNull();
	}
	
	@Test
	public void testOneRule() {
		assertThat(new FirstFinder<String>().addRule(() -> true, "dupa").find()).isEqualTo("dupa");
		assertThat(new FirstFinder<String>().addRule(() -> false, "dupa").find()).isNull();
		assertThat(new FirstFinder<String>().addRule(() -> false, "dupa")
				.addRule("dup")
				.find()).isEqualTo("dup");
	}
	
	@Test
	public void testManyRules() {
		assertThat(new FirstFinder<String>().addRule(() -> false, "dupa1")
				.addRule(() -> 1 == 2, "dupa2")
				.addRule(() -> 2 * 2 == 4, "dupa3")
				.addRule(() -> true, "dupa4")
				.addRule("def")
				.find()).isEqualTo("dupa3");
	}
}
