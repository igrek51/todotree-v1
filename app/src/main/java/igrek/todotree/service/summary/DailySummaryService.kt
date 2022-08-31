package igrek.todotree.service.summary

import android.content.Context
import com.google.common.base.Joiner
import igrek.todotree.info.logger.LoggerFactory.logger
import igrek.todotree.info.logger.Logger.debug
import igrek.todotree.service.statistics.StatisticsLogService.getLast24hEvents
import igrek.todotree.domain.stats.StatisticEvent.datetime
import igrek.todotree.domain.stats.StatisticEvent.type
import igrek.todotree.domain.stats.StatisticEvent.taskName
import igrek.todotree.info.logger.Logger.error
import igrek.todotree.service.summary.NotificationService
import igrek.todotree.service.statistics.StatisticsLogService
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.domain.stats.StatisticEvent
import java.lang.StringBuilder
import java.util.Collections
import java.util.Comparator
import igrek.todotree.domain.stats.StatisticEventType
import java.util.ArrayList
import java.lang.Exception

class DailySummaryService(
    private val context: Context,
    private val notificationService: NotificationService,
    private val statisticsLogService: StatisticsLogService
) {
    private val logger = LoggerFactory.logger
    fun showSummaryNotification() {
        val message = message
        if (message != null) {
            notificationService.sendNotification(context, "Daily summary", message)
        } else {
            logger.debug("no summary to show")
        }
    }//					message.append(" - ").append(datetimeFormat.format(event.getDatetime()));// no message

    // build statistics info
    private val message:
    // latest first
            String?
        private get() = try {
            val events = statisticsLogService.getLast24hEvents()
            // build statistics info
            val message = StringBuilder()
            var completed = 0
            var created = 0
            // latest first
            Collections.sort(events) { (_, datetime): StatisticEvent, (_, datetime1): StatisticEvent ->
                datetime1.compareTo(
                    datetime
                )
            }
            for ((type) in events) {
                if (type == StatisticEventType.TASK_COMPLETED) completed++ else if (type == StatisticEventType.TASK_CREATED) created++
            }
            if (!isSummaryToBeShown(completed, created)) {
                // no message
                return null
            }
            val diff = created - completed
            message.append("Recently completed tasks ($completed, diff: $diff):\n")
            val completedNames: MutableList<String?> = ArrayList()
            for ((type, _, taskName) in events) {
                if (type == StatisticEventType.TASK_COMPLETED) {
                    completedNames.add(taskName)
                    //					message.append(" - ").append(datetimeFormat.format(event.getDatetime()));
                }
            }
            Collections.reverse(completedNames)
            message.append(Joiner.on("; ").join(completedNames))
            message.toString()
        } catch (e: Exception) {
            logger.error(e)
            null
        }

    private fun isSummaryToBeShown(completed: Int, created: Int): Boolean {
        return completed > created
    }

    companion object {
        const val DAILY_SUMMARY_ACTION = "dailySummary"
    }
}