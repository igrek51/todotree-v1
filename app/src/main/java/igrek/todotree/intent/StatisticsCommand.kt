package igrek.todotree.intent

import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.domain.treeitem.TextTreeItem
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.service.statistics.StatisticsLogService

class StatisticsCommand(
    statisticsLogService: LazyInject<StatisticsLogService> = appFactory.statisticsLogService,
) {
    private val statisticsLogService by LazyExtractor(statisticsLogService)

    companion object {
        private const val keepStatisticsRegistry = false
    }

    fun onTaskCreated(item: AbstractTreeItem) {
        if (!keepStatisticsRegistry)
            return
        if (item is TextTreeItem) {
            statisticsLogService.logTaskCreate(item.displayName)
        }
    }

    fun onTaskRemoved(item: AbstractTreeItem) {
        if (!keepStatisticsRegistry)
            return
        if (item is TextTreeItem) {
            // log item and its children
            statisticsLogService.logTaskComplete(item.displayName)
            for (child in item.children) {
                onTaskRemoved(child)
            }
        }
    }

}