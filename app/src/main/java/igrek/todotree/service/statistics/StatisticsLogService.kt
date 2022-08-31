package igrek.todotree.service.statistics

import igrek.todotree.domain.stats.StatisticEvent
import igrek.todotree.domain.stats.StatisticEvent.Companion.parse
import igrek.todotree.domain.stats.StatisticEventType
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.service.filesystem.FilesystemService
import igrek.todotree.service.filesystem.removeExtension
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class StatisticsLogService(
    filesystemService: LazyInject<FilesystemService> = appFactory.filesystemService,
) {
    private val filesystemService by LazyExtractor(filesystemService)

    private val logger = LoggerFactory.logger

    private fun getTodayLog(): File {
        val logFile = getLogFile(Date())
        if (!logFile.exists()) logFile.createNewFile()
        return logFile
    }

    private fun getLogFile(date: Date): File {
        return getLogsDir().resolve(LOG_FILENAME_PREFIX + filenameDateFormat.format(date) + LOG_FILENAME_SUFFIX)
    }

    fun logTaskCreate(taskName: String?) {
        logTaskChange(taskName, StatisticEventType.TASK_CREATED)
    }

    fun logTaskComplete(taskName: String?) {
        logTaskChange(taskName, StatisticEventType.TASK_COMPLETED)
    }

    private fun logTaskChange(taskName: String?, type: StatisticEventType) {
        try {
            val todayLog = getTodayLog()
            val line = StringBuilder()
            line.append(type.givenName).append("\t")
            line.append(lineDateFormat.format(Date())).append("\t")
            line.append(taskName)
            appendLine(todayLog, line.toString())
            cleanUpLogs()
        } catch (e: IOException) {
            logger.error(e)
        }
    }

    @Throws(IOException::class)
    private fun appendLine(file: File, line: String) {
        FileWriter(file, true).use { fw ->
            BufferedWriter(fw).use { bw ->
                bw.write("""
    $line
    
    """.trimIndent())
            }
        }
        logger.debug("stats log line appended: $line")
    }

    private fun cleanUpLogs() {
        val logsDirPath = getLogsDir()
        val logs: List<String> = filesystemService.listDirFilenames(logsDirPath)
        // minimal keeping date = today minus keepDays
        val c = Calendar.getInstance()
        c.time = Date()
        c.add(Calendar.DATE, -KEEP_LOGS_DAYS)
        val minDate = c.time
        for (filename in logs) {
            if (filename.startsWith(LOG_FILENAME_PREFIX)) {
                val datePart = removeExtension(filename)
                        .substring(LOG_FILENAME_PREFIX.length)
                try {
                    val date = filenameDateFormat.parse(datePart)
                    if (date.before(minDate)) {
                        // need to be removed
                        removeLog(logsDirPath, filename)
                    }
                } catch (e: ParseException) {
                    logger.warn("Invalid date format in file name: $filename")
                }
            }
        }
    }

    private fun removeLog(path: File, filename: String) {
        path.resolve(filename).delete()
        logger.debug("log file removed: $filename")
    }

    private fun getLogsDir(): File {
        return filesystemService.appDataSubDir(LOGS_SUBDIR)
    }

    companion object {
        private const val KEEP_LOGS_DAYS = 14
        private const val LOGS_SUBDIR = "stats"
        private const val LOG_FILENAME_PREFIX = "stats-"
        private const val LOG_FILENAME_SUFFIX = ".log"
        private val filenameDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        private val lineDateFormat = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.ENGLISH)
    }

}