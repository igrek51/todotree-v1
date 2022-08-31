package igrek.todotree.service.access

import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.service.filesystem.FilesystemService
import igrek.todotree.service.filesystem.PathBuilder
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class AccessLogService(
    filesystemService: LazyInject<FilesystemService> = appFactory.filesystemService,
) {
    private val filesystemService by LazyExtractor(filesystemService)

    private val filenameDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    private val lineDateFormat = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.ENGLISH)
    private val logger = LoggerFactory.logger

    /**
     * @return current access log file
     */
    private val todayLog: File
        get() {
            val dir = accessLogDir
            val filename = ACCESS_LOG_PREFIX + filenameDateFormat.format(Date()) + ACCESS_LOG_SUFFIX
            val logFile = File(dir, filename)
            if (!logFile.exists()) {
                logger.debug("creating today log: " + logFile.absolutePath)
                logFile.createNewFile()
            }
            return logFile
        }

    /**
     * logs database unlock event
     */
    fun logDBUnlocked(item: AbstractTreeItem?) {
        try {
            val todayLog = todayLog
            val line = StringBuilder()
            line.append("db-unlocked\t")
            line.append(lineDateFormat.format(Date()))
            if (item != null) line.append("\t" + item.displayName)
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
    }

    /**
     * removes old access logs
     */
    private fun cleanUpLogs() {
        val accessDir = accessLogDir
        val logs = filesystemService.listDirFilenames(accessDir)
        // minimal keeping date = today minus keepDays
        val c = Calendar.getInstance()
        c.time = Date()
        c.add(Calendar.DATE, -ACCESS_LOGS_DAYS)
        val minDate = c.time
        for (filename in logs) {
            if (filename.startsWith(ACCESS_LOG_PREFIX)) {
                val datePart = PathBuilder.removeExtension(filename)
                        .substring(ACCESS_LOG_PREFIX.length)
                try {
                    val date = filenameDateFormat.parse(datePart)
                    if (date.before(minDate)) {
                        // need to be removed
                        removeLog(accessDir, filename)
                    }
                } catch (e: ParseException) {
                    logger.warn("Invalid date format in file name: $filename")
                }
            }
        }
    }

    private fun removeLog(dir: File, filename: String) {
        File(dir, filename).delete()
    }

    private val accessLogDir: File
        get() = filesystemService.appDataSubDir(ACCESS_LOGS_SUBDIR)

    companion object {
        private const val ACCESS_LOGS_DAYS = 14
        private const val ACCESS_LOGS_SUBDIR = "access"
        private const val ACCESS_LOG_PREFIX = "access-"
        private const val ACCESS_LOG_SUFFIX = ".log"
    }

}