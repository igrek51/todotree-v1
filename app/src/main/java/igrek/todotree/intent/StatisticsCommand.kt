package igrek.todotree.intent

import igrek.todotree.info.logger.LoggerFactory.logger
import igrek.todotree.dagger.FactoryComponent.inject
import igrek.todotree.service.statistics.StatisticsLogService.logTaskCreate
import igrek.todotree.domain.treeitem.AbstractTreeItem.displayName
import igrek.todotree.service.statistics.StatisticsLogService.logTaskComplete
import igrek.todotree.domain.treeitem.AbstractTreeItem.getChildren
import igrek.todotree.service.statistics.StatisticsLogService.getLast24hEvents
import igrek.todotree.domain.stats.StatisticEvent.datetime
import igrek.todotree.domain.stats.StatisticEvent.type
import igrek.todotree.domain.stats.StatisticEvent.taskName
import igrek.todotree.info.logger.Logger.error
import javax.inject.Inject
import igrek.todotree.service.statistics.StatisticsLogService
import android.app.Activity
import android.app.AlertDialog
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.domain.treeitem.TextTreeItem
import igrek.todotree.domain.stats.StatisticEvent
import java.lang.StringBuilder
import java.util.Collections
import java.util.Comparator
import igrek.todotree.domain.stats.StatisticEventType
import igrek.todotree.intent.StatisticsCommand
import android.content.DialogInterface
import java.lang.Exception
import java.util.Locale
import igrek.todotree.dagger.DaggerIoc
import java.text.SimpleDateFormat

class StatisticsCommand {
    @JvmField
	@Inject
    var statisticsLogService: StatisticsLogService? = null

    @JvmField
	@Inject
    var activity: Activity? = null
    private val logger = LoggerFactory.logger
    fun onTaskCreated(item: AbstractTreeItem) {
        if (item is TextTreeItem) {
            statisticsLogService!!.logTaskCreate(item.displayName)
        }
    }

    fun onTaskRemoved(item: AbstractTreeItem) {
        if (item is TextTreeItem) {
            // log item and its children
            statisticsLogService!!.logTaskComplete(item.displayName)
            for (child in item.getChildren()) {
                onTaskRemoved(child)
            }
        }
    }

    fun showStatisticsInfo() {
        try {
            val dlgAlert = AlertDialog.Builder(activity)
            val events = statisticsLogService!!.getLast24hEvents()
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

    init {
        DaggerIoc.factoryComponent.inject(this)
    }
}