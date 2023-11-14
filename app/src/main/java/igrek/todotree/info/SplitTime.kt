package igrek.todotree.info

import igrek.todotree.info.logger.Logger
import igrek.todotree.info.logger.LoggerFactory

class SplitTime {
    private val debug: Boolean = true
    private var lastTime: Long = 0
    private val logger: Logger = LoggerFactory.logger
    private val durationHistory: MutableMap<String, MutableList<Long>> = mutableMapOf()

    fun split(context: String) {
        if (!debug) return
        val now = System.currentTimeMillis()
        when (lastTime) {
            0L -> {
                logger.debug("Split: $context")
            }
            else -> {
                val duration = now - lastTime

                val durations = durationHistory.getOrPut(context) { mutableListOf() }
                durations.add(duration)
                val meanDuration = durations.average()
                val meanStr = "%.2f".format(meanDuration)
                val median = median(durations)

                logger.debug("Split: $context: ${duration}ms | median: ${median}ms, avg: ${meanStr}ms, ${durations.size} samples")
            }
        }
        lastTime = now
    }
}

val splitTime = SplitTime()

fun median(list: List<Long>) = list.sorted().let {
    if (it.size % 2 == 0)
        (it[it.size / 2] + it[(it.size - 1) / 2]) / 2
    else
        it[it.size / 2]
}