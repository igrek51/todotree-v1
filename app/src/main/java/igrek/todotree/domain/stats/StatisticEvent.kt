package igrek.todotree.domain.stats

import java.text.DateFormat
import java.util.*

data class StatisticEvent(val type: StatisticEventType, val datetime: Date, val taskName: String) {

    companion object {
        fun parse(line: String, dateFormat: DateFormat): StatisticEvent {
            var line = line
            line = line.trim { it <= ' ' }
            val parts = line.split("\t").toTypedArray()
            val type = StatisticEventType.parse(parts[0])
            val datetime = dateFormat.parse(parts[1])
            val taskName = parts[2]
            return StatisticEvent(type, datetime, taskName)
        }
    }

}