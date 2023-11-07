package igrek.todotree.info

import igrek.todotree.info.logger.Logger
import igrek.todotree.info.logger.LoggerFactory

class SplitTime {
    private var lastTime: Long = 0
    private val logger: Logger = LoggerFactory.logger
    private val durationHistory: MutableMap<String, MutableList<Long>> = mutableMapOf()

    fun split(context: String) {
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

                logger.debug("Split: $context: $duration ms, mean: $meanStr ms (${durations.size})")
            }
        }
        lastTime = now
    }
}

val splitTime = SplitTime()
