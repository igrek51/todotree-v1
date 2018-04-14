package igrek.todotree.domain.stats;

import java.text.ParseException;

public enum StatisticEventType {
	
	TASK_CREATED("task-created"),
	
	TASK_COMPLETED("task-completed");
	
	private String name;
	
	StatisticEventType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public static StatisticEventType parse(String name) throws ParseException {
		for (StatisticEventType statisticEventType : values()) {
			if (statisticEventType.name.equals(name))
				return statisticEventType;
		}
		throw new ParseException("unknown statistic event type name: " + name, 0);
	}
}