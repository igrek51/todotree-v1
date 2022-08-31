package igrek.todotree.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import igrek.todotree.info.logger.Logger
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.AppContextFactory
import igrek.todotree.service.filesystem.FilesystemService
import igrek.todotree.service.statistics.StatisticsLogService
import igrek.todotree.service.summary.DailySummaryService
import igrek.todotree.service.summary.NotificationService

class DailyReceiver : BroadcastReceiver() {

    private val logger: Logger = LoggerFactory.logger

    override fun onReceive(context: Context, intent: Intent) {
        logger.debug("received Daily Summary request")
        try {
            logger.info("Creating Dependencies container...")
            AppContextFactory.createAppContext(context)
        } catch (t: Throwable) {
            logger.fatal(t)
            throw t
        }
        val filesystem = FilesystemService(context)
        val notificationService = NotificationService()
        val statisticsLogService = StatisticsLogService(filesystem)
        val dailySummaryService = DailySummaryService(context, notificationService, statisticsLogService)
        dailySummaryService.showSummaryNotification()
        logger.debug("Daily Summary has been ended")
    }
}