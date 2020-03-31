package igrek.todotree.mock

import igrek.todotree.info.logger.LogLevel
import igrek.todotree.info.logger.Logger

class LoggerMock : Logger() {
    override fun fatal(ex: Throwable) {
        log(ex.message, LogLevel.FATAL, "[FATAL] ")
    }

    override fun printInfo(msg: String) {
        println(msg)
    }

    override fun printError(msg: String) {
        System.err.println(msg)
    }
}