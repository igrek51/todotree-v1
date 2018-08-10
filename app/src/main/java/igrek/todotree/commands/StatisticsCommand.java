package igrek.todotree.commands;

import android.app.Activity;
import android.app.AlertDialog;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.domain.stats.StatisticEvent;
import igrek.todotree.domain.stats.StatisticEventType;
import igrek.todotree.domain.treeitem.AbstractTreeItem;
import igrek.todotree.domain.treeitem.TextTreeItem;
import igrek.todotree.logger.Logger;
import igrek.todotree.service.statistics.StatisticsLogService;

//TODO when deleted add event to interim buffer only, save to log when db is really saved
public class StatisticsCommand {
	
	private static final SimpleDateFormat displayDateFormat = new SimpleDateFormat("HH:mm:ss, dd.MM.yyyy", Locale.ENGLISH);
	
	@Inject
	StatisticsLogService statisticsLogService;
	
	@Inject
	Activity activity;
	
	@Inject
	Logger logger;
	
	public StatisticsCommand() {
		DaggerIOC.getFactoryComponent().inject(this);
	}
	
	public void onTaskCreated(AbstractTreeItem item) {
		if (item instanceof TextTreeItem) {
			statisticsLogService.logTaskCreate(item.getDisplayName());
		}
	}
	
	public void onTaskRemoved(AbstractTreeItem item) {
		if (item instanceof TextTreeItem) {
			// log item and its children
			statisticsLogService.logTaskComplete(item.getDisplayName());
			for (AbstractTreeItem child : item.getChildren()) {
				onTaskRemoved(child);
			}
		}
	}
	
	public void showStatisticsInfo() {
		try {
			AlertDialog.Builder dlgAlert = new AlertDialog.Builder(activity);
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
			
			message.append("Last 24h statistics");
			message.append("\nNew: " + created);
			message.append("\nCompleted: " + completed);
			message.append("\nDiff: " + (created - completed));
			
			if (completed > created) {
				message.append("\nCongratulations ! :)");
			}
			
			message.append("\n\nLast completed tasks (" + completed + "):");
			for (StatisticEvent event : events) {
				if (event.getType().equals(StatisticEventType.TASK_COMPLETED)) {
					message.append("\n");
					message.append(event.getTaskName());
					message.append(" - ").append(displayDateFormat.format(event.getDatetime()));
				}
			}
			
			message.append("\n\nRecently created tasks (" + created + "):");
			for (StatisticEvent event : events) {
				if (event.getType().equals(StatisticEventType.TASK_CREATED)) {
					message.append("\n");
					message.append(event.getTaskName());
					message.append(" - ").append(displayDateFormat.format(event.getDatetime()));
				}
			}
			
			dlgAlert.setMessage(message.toString());
			dlgAlert.setTitle("Statistics");
			dlgAlert.setPositiveButton("OK", (dialog, which) -> {
			});
			dlgAlert.setCancelable(true);
			dlgAlert.create().show();
		} catch (Exception e) {
			logger.error(e);
		}
	}
}
