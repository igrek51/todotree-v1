package igrek.todotree.services.filesystem;

import java.util.LinkedHashMap;

public class FirstFinder<T> {
	
	// remembers inserting order
	private LinkedHashMap<BooleanCondition, T> rules = new LinkedHashMap<>();
	
	public FirstFinder() {
	}
	
	public FirstFinder<T> addRule(BooleanCondition when, T then) {
		rules.put(when, then);
		return this;
	}
	
	public FirstFinder<T> addRule(T then) {
		return addRule(() -> true, then);
	}
	
	public T find() {
		for (BooleanCondition when : rules.keySet()) {
			if (when.test()) {
				T then = rules.get(when);
				if (then != null) // accept only not null values
					return then;
			}
		}
		return null;
	}
	
	@FunctionalInterface
	public interface BooleanCondition {
		
		boolean test();
	}
}
