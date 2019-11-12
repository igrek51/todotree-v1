package igrek.todotree.service.summary;

import android.app.Activity;

import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import igrek.todotree.domain.stats.StatisticEvent;
import igrek.todotree.domain.stats.StatisticEventType;
import igrek.todotree.logger.Logger;
import android.content.Context;
import igrek.todotree.logger.LoggerFactory;
import igrek.todotree.service.statistics.StatisticsLogService;

public class DailySummaryService {
	
	public static final String DAILY_SUMMARY_ACTION = "dailySummary";
	
	private Logger logger = LoggerFactory.getLogger();
	private Context context;
	private NotificationService notificationService;
	private StatisticsLogService statisticsLogService;
	
	public DailySummaryService(Context context, NotificationService notificationService, StatisticsLogService statisticsLogService) {
		this.context = context;
		this.notificationService = notificationService;
		this.statisticsLogService = statisticsLogService;
	}
	
	public void showSummaryNotification() {
		String message = getMessage();
		if (message != null) {
			notificationService.sendNotification(context, "Daily summary", message);
		} else {
			logger.debug("no summary to show");
		}
	}
	
	private String getMessage() {
		try {
			List<StatisticEvent> events = statisticsLogService.getLast24hEvents();
			// build statistics info
			StringBuilder message = new StringBuilder();
			int completed = 0;
			int created = 0;
			// latest first
			Collections.sort(events, (o1, o2) -> o2.getDatetime().compareTo(o1.getDatetime()));
			for (StatisticEvent event : events) {
				if (event.getType().equals(StatisticEventType.TASK_COMPLETED))
					completed++;
				else if (event.getType().equals(StatisticEventType.TASK_CREATED))
					created++;
			}
			
			if (!isSummaryToBeShown(completed, created)) {
				// no message
				return null;
			}
			
			int diff = created - completed;
			
			message.append("You have done a lot today :) (diff: " + diff + ").\n");
			
			message.append("Recently completed tasks (" + completed + "):\n");
			List<String> completedNames = new ArrayList<>();
			for (StatisticEvent event : events) {
				if (event.getType().equals(StatisticEventType.TASK_COMPLETED)) {
					completedNames.add(event.getTaskName());
					//					message.append(" - ").append(datetimeFormat.format(event.getDatetime()));
				}
			}
			Collections.reverse(completedNames);
			message.append(Joiner.on("; ").join(completedNames));
			
			return message.toString();
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	private boolean isSummaryToBeShown(int completed, int created) {
		return completed > created;
	}
}
