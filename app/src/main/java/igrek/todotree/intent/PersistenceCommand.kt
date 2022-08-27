package igrek.todotree.intent

import igrek.todotree.dagger.DaggerIoc
import igrek.todotree.exceptions.DeserializationFailedException
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.service.backup.Backup
import igrek.todotree.service.backup.BackupManager
import igrek.todotree.service.filesystem.FilesystemService
import igrek.todotree.service.history.ChangesHistory
import igrek.todotree.service.preferences.Preferences
import igrek.todotree.service.resources.UserInfoService
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.service.tree.TreeScrollCache
import igrek.todotree.service.tree.persistence.TreePersistenceService
import igrek.todotree.ui.contextmenu.BackupListMenu
import java.io.File
import java.io.IOException
import javax.inject.Inject

class PersistenceCommand {
    @Inject
    lateinit var treeManager: TreeManager

    @Inject
    lateinit var userInfo: UserInfoService

    @Inject
    lateinit var backupManager: BackupManager

    @Inject
    lateinit var scrollCache: TreeScrollCache

    @Inject
    lateinit var filesystem: FilesystemService

    @Inject
    lateinit var preferences: Preferences

    @Inject
    lateinit var persistenceService: TreePersistenceService

    @Inject
    lateinit var changesHistory: ChangesHistory

    private val logger = LoggerFactory.logger

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun optionReload() {
        treeManager.reset()
        scrollCache.clear()
        loadRootTree()
        GUICommand().updateItemsList()
        userInfo.showInfo("Database loaded.")
    }

    fun optionSave() {
        val saved = saveDatabase()
        if (saved)
            userInfo.showInfo("Database saved.")
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
        val appDataDir = filesystem.appDataRootDir()
        return appDataDir.resolve("todo.json")
    }

    private fun dbCopyFile(): File {
        return filesystem.internalStorageAppDir().resolve("todo.json")
    }

    fun loadRootTreeFromBackup(backup: Backup) {
        val backupDir = filesystem.appDataSubDir("backup")
        val path = backupDir.resolve(backup.filename)
        loadDbFromFile(path)
        changesHistory.registerChange()
    }

    private fun loadDbFromFile(dbFile: File) {
        changesHistory.clear()
        logger.info("Loading database from file: $dbFile")
        if (!dbFile.exists()) {
            userInfo.showInfo("Database file does not exist. Default empty database loaded.")
            return
        }
        try {
            val fileContent = filesystem.openFileString(dbFile.absolutePath)
            // AbstractTreeItem rootItem = SimpleTreeSerializer.loadTree(fileContent); // porting db to JSON
            val rootItem = persistenceService.deserializeTree(fileContent)
            treeManager.rootItem = rootItem
            logger.info("Database loaded.")
        } catch (e: IOException) {
            changesHistory.registerChange()
            logger.error(e)
            userInfo.showInfo("Failed to load database: " + e.message)
        } catch (e: DeserializationFailedException) {
            changesHistory.registerChange()
            logger.error(e)
            userInfo.showInfo("Failed to load database: " + e.message)
        }
    }

    private fun saveRootTree() {
        try {
            val output = persistenceService.serializeTree(treeManager.rootItem)
            //Logger.debug("Serialized data: " + output);
            val dbFilePath = dbFile().absolutePath
            filesystem.saveFile(dbFilePath, output)
            filesystem.saveFile(dbCopyFile().absolutePath, output)
            logger.debug("Database saved successfully to $dbFilePath")
        } catch (e: IOException) {
            logger.error(e)
        }
    }

    fun optionRestoreBackup() {
        BackupListMenu().show()
    }

}