package igrek.todotree.info

import igrek.todotree.info.logger.Logger
import igrek.todotree.info.logger.LoggerFactory

class SplitTime {
    private var lastTime: Long = 0
    private val logger: Logger = LoggerFactory.logger

    fun split(context: String) {
        val now = System.currentTimeMillis()
        when (lastTime) {
            0L -> {
                logger.debug("Split: $context")
            }
            else -> {
                val duration = now - lastTime
                logger.debug("Split: $context: $duration ms")
            }
        }
        lastTime = now
    }
}

val splitTime = SplitTime()
