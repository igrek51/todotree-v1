package igrek.todotree.intent

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import igrek.todotree.domain.stats.StatisticEvent
import igrek.todotree.domain.stats.StatisticEventType
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.domain.treeitem.TextTreeItem
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.service.statistics.StatisticsLogService
import java.text.SimpleDateFormat
import java.util.*

class StatisticsCommand(
    statisticsLogService: LazyInject<StatisticsLogService> = appFactory.statisticsLogService,
    activity: LazyInject<Activity> = appFactory.activityMust,
) {
    private val statisticsLogService by LazyExtractor(statisticsLogService)
    private val activity by LazyExtractor(activity)

    private val logger = LoggerFactory.logger
    fun onTaskCreated(item: AbstractTreeItem) {
        if (item is TextTreeItem) {
            statisticsLogService.logTaskCreate(item.displayName)
        }
    }

    fun onTaskRemoved(item: AbstractTreeItem) {
        if (item is TextTreeItem) {
            // log item and its children
            statisticsLogService.logTaskComplete(item.displayName)
            for (child in item.getChildren()) {
                onTaskRemoved(child)
            }
        }
    }

    fun showStatisticsInfo() {
        try {
            val dlgAlert = AlertDialog.Builder(activity)
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
            message.append("Last 24h statistics")
            message.append("\nNew: $created")
            message.append("\nCompleted: $completed")
            message.append(
                """
    
    Diff: ${created - completed}
    """.trimIndent()
            )
            if (completed > created) {
                message.append("\nCongratulations ! :)")
            }
            message.append("\n\nLast completed tasks ($completed):")
            for ((type, datetime, taskName) in events) {
                if (type == StatisticEventType.TASK_COMPLETED) {
                    message.append("\n")
                    message.append(taskName)
                    message.append(" - ").append(
                        displayDateFormat.format(
                            datetime
                        )
                    )
                }
            }
            message.append("\n\nRecently created tasks ($created):")
            for ((type, datetime, taskName) in events) {
                if (type == StatisticEventType.TASK_CREATED) {
                    message.append("\n")
                    message.append(taskName)
                    message.append(" - ").append(
                        displayDateFormat.format(
                            datetime
                        )
                    )
                }
            }
            dlgAlert.setMessage(message.toString())
            dlgAlert.setTitle("Statistics")
            dlgAlert.setPositiveButton("OK") { dialog: DialogInterface?, which: Int -> }
            dlgAlert.setCancelable(true)
            dlgAlert.create().show()
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    companion object {
        private val displayDateFormat = SimpleDateFormat("HH:mm:ss, dd.MM.yyyy", Locale.ENGLISH)
    }
}