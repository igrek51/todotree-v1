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
		Number nil = null;
		assertThat(new FirstFinder<Number>().addRule(nil).find()).isNull();
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
	
	@Test
	public void testManyRulesOrder() {
		assertThat(new FirstFinder<String>().addRule(() -> false, "dupa1")
				.addRule(() -> false, "dupa2")
				.addRule(() -> true, "dupa3")
				.addRule(() -> true, "dupa4")
				.addRule(() -> true, "dupa5")
				.find()).isEqualTo("dupa3");
		assertThat(new FirstFinder<String>().addRule(() -> false, "dupa1")
				.addRule(() -> true, "dupa2")
				.addRule(() -> false, "dupa3")
				.addRule(() -> false, "dupa4")
				.addRule(() -> false, "dupa5")
				.addRule(() -> false, "dupa6")
				.addRule(() -> true, "dupa7")
				.find()).isEqualTo("dupa2");
	}
	
	@Test
	public void testProviders() {
		assertThat(new FirstFinder<String>().addRule(() -> "Dupa").find()).isEqualTo("Dupa");
		assertThat(new FirstFinder<String>().addRule(() -> false, () -> "dupa1")
				.addRule(() -> 1 == 2, "dupa2")
				.addRule(() -> 2 * 2 == 4, () -> "dupa3")
				.addRule(() -> true, "dupa4")
				.addRule(() -> "def")
				.find()).isEqualTo("dupa3");
	}
}
