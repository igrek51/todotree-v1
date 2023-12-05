package igrek.todotree.info.logger

data class LogEntry(
    val message: String,
    val timestampS: Long,
    val level: LogLevel,
)
