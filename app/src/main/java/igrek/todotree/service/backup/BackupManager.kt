package igrek.todotree.service.backup

import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.service.filesystem.FilesystemService
import igrek.todotree.service.filesystem.removeExtension
import java.io.File
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class BackupManager(
    filesystemService: LazyInject<FilesystemService> = appFactory.filesystemService,
) {
    private val filesystem by LazyExtractor(filesystemService)

    private val dateFormat = SimpleDateFormat("yyyy_MM_dd-HH_mm_ss", Locale.ENGLISH)
    private val logger = LoggerFactory.logger

    fun saveBackupFile() {
        val backupsDir = filesystem.appDataSubDir(BACKUP_SUBDIR)
        val originalDbFile = filesystem.appDataRootDir().resolve("todo.json")
        saveNewBackup(originalDbFile, backupsDir)
        removeOldBackups(backupsDir)
    }

    private fun saveNewBackup(originalDbFile: File, backupsDir: File) {
        val backupFile = backupsDir.resolve(BACKUP_FILE_PREFIX + dateFormat.format(Date()))
        try {
            filesystem.copy(originalDbFile, backupFile)
            logger.info("Backup created: $backupFile")
        } catch (e: IOException) {
            logger.error(e)
        }
    }

    private fun removeOldBackups(backupsDir: File) {
        // backup files list to remove
        val backups = getBackups()

        // retain few newest backups
        var i = 0
        while (i < BACKUP_LAST_VERSIONS && backups.isNotEmpty()) {
            backups.removeAt(0)
            i++
        }

        // retain backups from different days
        for (i in 1..BACKUP_LAST_DAYS) {
            // calculate days before
            val calendar1 = Calendar.getInstance()
            calendar1.time = Date()
            calendar1.add(Calendar.DAY_OF_MONTH, -i)
            // find newest backup from that day
            for (j in backups.indices) {
                val backup = backups[j]
                if (isSameDay(calendar1, backup.date)) {
                    backups.removeAt(j) // and retain this backup
                    break
                }
            }
        }

        // remove other backups
        for (backup in backups) {
            val toRemovePath = backupsDir.resolve(backup.filename)
            toRemovePath.delete()
            logger.info("Old backup has been removed: $toRemovePath")
        }
    }

    // recognize backup files and read date from name
    fun getBackups(): MutableList<Backup> {
        val backupsDir = filesystem.appDataSubDir(BACKUP_SUBDIR)
        val children: List<String> = filesystem.listDirFilenames(backupsDir)
        val backups: MutableList<Backup> = ArrayList()
        // recognize bbackup files and read date from name
        for (filename in children) {
            if (filename.startsWith(BACKUP_FILE_PREFIX)) {
                val dateStr = removeExtension(filename)
                        .substring(BACKUP_FILE_PREFIX.length)
                var date: Date?
                try {
                    date = dateFormat.parse(dateStr)
                    backups.add(Backup(filename, date))
                } catch (e: ParseException) {
                    logger.warn("Invalid date format in file name: $filename")
                }
            }
        }
        backups.sort()
        return backups
    }

    private fun isSameDay(cal1: Calendar, date2: Date): Boolean {
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        return cal1[Calendar.YEAR] == cal2[Calendar.YEAR] && cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR]
    }

    companion object {
        private const val BACKUP_FILE_PREFIX = "backup_"

        /** max count of last versions backups  */
        private const val BACKUP_LAST_VERSIONS = 10

        /** daily backups count  */
        private const val BACKUP_LAST_DAYS = 14

        /** daily backups count  */
        private const val BACKUP_SUBDIR = "backup"
    }

}