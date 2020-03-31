package igrek.todotree.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import igrek.todotree.info.logger.Logger
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.service.filesystem.FilesystemService
import igrek.todotree.service.preferences.Preferences
import igrek.todotree.service.statistics.StatisticsLogService
import igrek.todotree.service.summary.DailySummaryService
import igrek.todotree.service.summary.NotificationService
import igrek.todotree.system.filesystem.ExternalCardService

class DailyReceiver : BroadcastReceiver() {

    private val logger: Logger = LoggerFactory.logger

    override fun onReceive(context: Context, intent: Intent) {
        logger.debug("received Daily Summary request")
        val externalCardService = ExternalCardService()
        val filesystem = FilesystemService(context, externalCardService)
        val preferences = Preferences(context)
        val notificationService = NotificationService()
        val statisticsLogService = StatisticsLogService(filesystem, preferences)
        val dailySummaryService = DailySummaryService(context, notificationService, statisticsLogService)
        dailySummaryService.showSummaryNotification()
        logger.debug("Daily Summary has been ended")
    }
}