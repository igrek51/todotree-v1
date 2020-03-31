package igrek.todotree.domain.stats

import java.text.ParseException

enum class StatisticEventType(val givenName: String) {
    TASK_CREATED("task-created"), TASK_COMPLETED("task-completed");

    companion object {
        @Throws(ParseException::class)
        fun parse(givenName: String): StatisticEventType {
            for (statisticEventType in values()) {
                if (statisticEventType.givenName == givenName) return statisticEventType
            }
            throw ParseException("unknown statistic event type name: $givenName", 0)
        }
    }

}