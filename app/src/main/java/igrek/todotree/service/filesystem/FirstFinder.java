package igrek.todotree.service.filesystem;

import java.util.LinkedList;
import java.util.List;

public class FirstFinder<T> {
	
	// remembers inserting order
	private List<Rule> rules = new LinkedList<>();
	
	public FirstFinder() {
	}
	
	public FirstFinder<T> addRule(BooleanCondition when, Provider<T> then) {
		rules.add(new Rule(when, then));
		return this;
	}
	
	public FirstFinder<T> addRule(BooleanCondition when, T then) {
		rules.add(new Rule(when, () -> then));
		return this;
	}
	
	public FirstFinder<T> addRule(Provider<T> then) {
		return addRule(() -> true, then);
	}
	
	public FirstFinder<T> addRule(T defaultValue) {
		return addRule(() -> true, () -> defaultValue);
	}
	
	public T find() {
		for (Rule rule : rules) {
			BooleanCondition when = rule.when;
			if (when.test()) {
				Provider<T> then = rule.then;
				if (then != null) {
					T value = then.get();
					if (value != null) // accept only not null values
						return value;
				}
			}
		}
		return null;
	}
	
	private class Rule {
		BooleanCondition when;
		Provider<T> then;
		
		public Rule(BooleanCondition when, Provider<T> then) {
			this.when = when;
			this.then = then;
		}
	}
	
	@FunctionalInterface
	public interface BooleanCondition {
		boolean test();
	}
	
	@FunctionalInterface
	public interface Provider<T> {
		T get();
	}
}
