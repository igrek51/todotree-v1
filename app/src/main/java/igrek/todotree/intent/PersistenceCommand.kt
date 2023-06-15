package igrek.todotree.intent

import igrek.todotree.exceptions.DeserializationFailedException
import igrek.todotree.info.UiInfoService
import igrek.todotree.info.errorcheck.SafeExecutor
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.service.backup.Backup
import igrek.todotree.service.backup.BackupManager
import igrek.todotree.service.filesystem.FilesystemService
import igrek.todotree.service.history.ChangesHistory
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.service.tree.TreeScrollCache
import igrek.todotree.service.tree.persistence.TreePersistenceService
import igrek.todotree.ui.contextmenu.BackupListMenu
import java.io.File
import java.io.IOException

class PersistenceCommand(
    treeManager: LazyInject<TreeManager> = appFactory.treeManager,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    backupManager: LazyInject<BackupManager> = appFactory.backupManager,
    treeScrollCache: LazyInject<TreeScrollCache> = appFactory.treeScrollCache,
    filesystemService: LazyInject<FilesystemService> = appFactory.filesystemService,
    treePersistenceService: LazyInject<TreePersistenceService> = appFactory.treePersistenceService,
    changesHistory: LazyInject<ChangesHistory> = appFactory.changesHistory,
) {
    private val treeManager by LazyExtractor(treeManager)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val backupManager by LazyExtractor(backupManager)
    private val treeScrollCache by LazyExtractor(treeScrollCache)
    private val filesystemService by LazyExtractor(filesystemService)
    private val treePersistenceService by LazyExtractor(treePersistenceService)
    private val changesHistory by LazyExtractor(changesHistory)
    private val settingsState by LazyExtractor(appFactory.settingsState)

    private val logger = LoggerFactory.logger

    fun optionReload() {
        SafeExecutor {
            treeManager.reset()
            treeScrollCache.clear()
            loadDbFromFile(dbFile())
            uiInfoService.showInfo("Database loaded.")
        }
        GUICommand().updateItemsList()
    }

    fun saveDatabaseUi() {
        val saved = saveDatabase()
        if (saved)
            uiInfoService.showInfo("Database saved.")
    }

    fun saveDatabase(): Boolean {
        if (!changesHistory.hasChanges()) {
            logger.info("No changes have been made - skipping saving")
            return false
        }
        saveRootTree()
        backupManager.saveBackupFile()
        changesHistory.clear()
        return true
    }

    fun loadRootTree() {
        SafeExecutor {
            loadDbFromFile(dbFile())
        }
    }

    private fun dbFile(): File {
        val appDataDir = filesystemService.appDataRootDir()
        return appDataDir.resolve("todo.json")
    }

    private fun dbCopyFile(): File {
        return filesystemService.internalStorageAppDir().resolve("todo.json")
    }

    private fun externalBackupFile(): File? {
        val path = settingsState.externalBackupPath.takeIf { it.isNotBlank() } ?: return null
        val dir = File(path)
        dir.mkdirs()
        return dir.resolve("todo.json")
    }

    fun loadRootTreeFromBackup(backup: Backup) {
        SafeExecutor {
            treeManager.reset()
            treeScrollCache.clear()
            val backupDir = filesystemService.appDataSubDir("backup")
            val path = backupDir.resolve(backup.filename)
            loadDbFromFile(path)
            changesHistory.registerChange()
            uiInfoService.showInfo("Database backup loaded: " + backup.filename)
        }
        GUICommand().updateItemsList()
    }

    fun loadRootTreeFromImportedFile(fileContent: String, filename: String) {
        SafeExecutor {
            treeManager.reset()
            treeScrollCache.clear()
            loadDbFromFileContent(fileContent)
            changesHistory.registerChange()
            uiInfoService.showInfo("Database imported from $filename")
        }
        GUICommand().updateItemsList()
    }

    private fun loadDbFromFile(dbFile: File) {
        changesHistory.clear()
        logger.debug("Loading database from file: $dbFile")
        if (!dbFile.exists()) {
            throw RuntimeException("Database file does not exist: $dbFile. Default empty database loaded.")
        }
        try {
            val fileContent = filesystemService.openFileString(dbFile.absolutePath)
            val rootItem = treePersistenceService.deserializeTree(fileContent)
            treeManager.rootItem = rootItem
            logger.info("Database loaded from file: $dbFile")
        } catch (e: IOException) {
            changesHistory.registerChange()
            throw RuntimeException("Failed to load database: " + e.message)
        } catch (e: DeserializationFailedException) {
            changesHistory.registerChange()
            throw RuntimeException("Failed to load database: " + e.message)
        }
    }

    private fun loadDbFromFileContent(fileContent: String) {
        changesHistory.clear()
        try {
            val rootItem = treePersistenceService.deserializeTree(fileContent)
            treeManager.rootItem = rootItem
        } catch (e: IOException) {
            changesHistory.registerChange()
            throw RuntimeException("Failed to load database: " + e.message)
        } catch (e: DeserializationFailedException) {
            changesHistory.registerChange()
            throw RuntimeException("Failed to load database: " + e.message)
        }
    }

    private fun saveRootTree() {
        try {
            val output = treePersistenceService.serializeTree(treeManager.rootItem)
            val dbFilePath = dbFile().absolutePath
            val internalBackupFilePath = dbCopyFile().absolutePath // backup copy in the internal storage
            val externalBackupFile = externalBackupFile() // backup copy in the external storage (optional)

            filesystemService.saveFile(dbFilePath, output)
            logger.info("Database saved to $dbFilePath")

            filesystemService.saveFile(internalBackupFilePath, output)
            logger.debug("Database copy saved to $internalBackupFilePath")

            if (externalBackupFile != null) {
                val externalBackupFilePath = externalBackupFile.absolutePath
                filesystemService.saveFile(externalBackupFilePath, output)
                logger.debug("Database copy saved to $externalBackupFilePath")
            }
        } catch (e: IOException) {
            logger.error(e)
        }
    }

    fun optionRestoreBackup() {
        BackupListMenu().show()
    }

}