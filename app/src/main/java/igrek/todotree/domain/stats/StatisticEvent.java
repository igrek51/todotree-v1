package igrek.todotree.domain.stats;

import java.text.DateFormat;
import java.util.Date;

public class StatisticEvent {
	
	private StatisticEventType type;
	private Date datetime;
	private String taskName;
	
	public StatisticEvent(StatisticEventType type, Date datetime, String taskName) {
		this.type = type;
		this.datetime = datetime;
		this.taskName = taskName;
	}
	
	public static StatisticEvent parse(String line, DateFormat dateFormat) throws Exception {
		line = line.trim();
		String[] parts = line.split("\t");
		StatisticEventType type = StatisticEventType.parse(parts[0]);
		Date datetime = dateFormat.parse(parts[1]);
		String taskName = parts[2];
		return new StatisticEvent(type, datetime, taskName);
	}
	
	public StatisticEventType getType() {
		return type;
	}
	
	public Date getDatetime() {
		return datetime;
	}
	
	public String getTaskName() {
		return taskName;
	}
}