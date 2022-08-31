package igrek.todotree.intent

import igrek.todotree.exceptions.DeserializationFailedException
import igrek.todotree.info.UiInfoService
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

    private val logger = LoggerFactory.logger

    fun optionReload() {
        treeManager.reset()
        treeScrollCache.clear()
        loadRootTree()
        GUICommand().updateItemsList()
        uiInfoService.showInfo("Database loaded.")
    }

    fun optionSave() {
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
        loadDbFromFile(dbFile())
    }

    private fun dbFile(): File {
        val appDataDir = filesystemService.appDataRootDir()
        return appDataDir.resolve("todo.json")
    }

    private fun dbCopyFile(): File {
        return filesystemService.internalStorageAppDir().resolve("todo.json")
    }

    fun loadRootTreeFromBackup(backup: Backup) {
        val backupDir = filesystemService.appDataSubDir("backup")
        val path = backupDir.resolve(backup.filename)
        loadDbFromFile(path)
        changesHistory.registerChange()
    }

    private fun loadDbFromFile(dbFile: File) {
        changesHistory.clear()
        logger.info("Loading database from file: $dbFile")
        if (!dbFile.exists()) {
            uiInfoService.showInfo("Database file does not exist. Default empty database loaded.")
            return
        }
        try {
            val fileContent = filesystemService.openFileString(dbFile.absolutePath)
            // AbstractTreeItem rootItem = SimpleTreeSerializer.loadTree(fileContent); // porting db to JSON
            val rootItem = treePersistenceService.deserializeTree(fileContent)
            treeManager.rootItem = rootItem
            logger.info("Database loaded.")
        } catch (e: IOException) {
            changesHistory.registerChange()
            logger.error(e)
            uiInfoService.showInfo("Failed to load database: " + e.message)
        } catch (e: DeserializationFailedException) {
            changesHistory.registerChange()
            logger.error(e)
            uiInfoService.showInfo("Failed to load database: " + e.message)
        }
    }

    private fun saveRootTree() {
        try {
            val output = treePersistenceService.serializeTree(treeManager.rootItem)
            //Logger.debug("Serialized data: " + output);
            val dbFilePath = dbFile().absolutePath
            filesystemService.saveFile(dbFilePath, output)
            filesystemService.saveFile(dbCopyFile().absolutePath, output)
            logger.debug("Database saved successfully to $dbFilePath")
        } catch (e: IOException) {
            logger.error(e)
        }
    }

    fun optionRestoreBackup() {
        BackupListMenu().show()
    }

}