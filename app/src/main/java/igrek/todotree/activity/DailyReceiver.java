package igrek.todotree.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import igrek.todotree.logger.Logger;
import igrek.todotree.logger.LoggerFactory;
import igrek.todotree.service.filesystem.ExternalCardService;
import igrek.todotree.service.filesystem.FilesystemService;
import igrek.todotree.service.preferences.Preferences;
import igrek.todotree.service.statistics.StatisticsLogService;
import igrek.todotree.service.summary.DailySummaryService;
import igrek.todotree.service.summary.NotificationService;

public class DailyReceiver extends BroadcastReceiver {
	
	private Logger logger = LoggerFactory.getLogger();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		logger.debug("received Daily Summary request");
		
		Logger logger = LoggerFactory.getLogger();
		ExternalCardService externalCardService = new ExternalCardService(logger);
		FilesystemService filesystem = new FilesystemService(logger, context, externalCardService);
		Preferences preferences = new Preferences(context, logger);
		NotificationService notificationService = new NotificationService();
		StatisticsLogService statisticsLogService = new StatisticsLogService(filesystem, preferences);
		DailySummaryService dailySummaryService = new DailySummaryService(context, notificationService, statisticsLogService);
		
		dailySummaryService.showSummaryNotification();
		
		logger.debug("Daily Summary has been ended");
	}
	
}