package igrek.todotree.commands;


import java.io.IOException;

import javax.inject.Inject;

import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.exceptions.DeserializationFailedException;
import igrek.todotree.logger.Logs;
import igrek.todotree.model.treeitem.AbstractTreeItem;
import igrek.todotree.services.backup.Backup;
import igrek.todotree.services.backup.BackupManager;
import igrek.todotree.services.filesystem.FilesystemService;
import igrek.todotree.services.filesystem.PathBuilder;
import igrek.todotree.services.history.ChangesHistory;
import igrek.todotree.services.preferences.Preferences;
import igrek.todotree.services.preferences.PropertyDefinition;
import igrek.todotree.services.resources.UserInfoService;
import igrek.todotree.services.tree.TreeManager;
import igrek.todotree.services.tree.TreeScrollCache;
import igrek.todotree.services.tree.persistence.TreePersistenceService;
import igrek.todotree.ui.contextmenu.BackupListMenu;

public class PersistenceCommand {
	
	@Inject
	TreeManager treeManager;
	
	@Inject
	UserInfoService userInfo;
	
	@Inject
	BackupManager backupManager;
	
	@Inject
	TreeScrollCache scrollCache;
	
	@Inject
	FilesystemService filesystem;
	
	@Inject
	Preferences preferences;
	
	@Inject
	TreePersistenceService persistenceService;
	
	@Inject
	ChangesHistory changesHistory;
	
	@Inject
	Logs logger;
	
	public PersistenceCommand() {
		DaggerIOC.getAppComponent().inject(this);
	}
	
	void optionReload() {
		treeManager.reset();
		scrollCache.clear();
		loadRootTree();
		new GUICommand().updateItemsList();
		userInfo.showInfo("Database loaded.");
	}
	
	void optionSave() {
		saveDatabase();
		userInfo.showInfo("Database saved.");
	}
	
	void saveDatabase() {
		if (!changesHistory.hasChanges()) {
			logger.info("No changes have been made - skipping saving");
			return;
		}
		
		saveRootTree();
		backupManager.saveBackupFile();
	}
	
	public void loadRootTree() {
		filesystem.mkdirIfNotExist(filesystem.externalAndroidDir().toString());
		String dbFilePath = preferences.getValue(PropertyDefinition.dbFilePath, String.class);
		if (dbFilePath.startsWith("/")) { //absolute
			loadDbFromFile(dbFilePath);
		} else { // relative
			PathBuilder dbFilePathBuilder = filesystem.externalAndroidDir().append(dbFilePath);
			loadDbFromFile(dbFilePathBuilder.toString());
		}
	}
	
	public void loadRootTreeFromBackup(Backup backup) {
		PathBuilder dbFilePath = filesystem.externalAndroidDir()
				.append(preferences.getValue(PropertyDefinition.dbFilePath, String.class));
		String path = dbFilePath.parent().append(backup.getFilename()).toString();
		loadDbFromFile(path);
		changesHistory.registerChange();
	}
	
	private void loadDbFromFile(String dbFilePath) {
		changesHistory.clear();
		logger.info("Loading database from file: " + dbFilePath);
		if (!filesystem.exists(dbFilePath)) {
			userInfo.showInfo("Database file does not exist. Default empty database loaded.");
			return;
		}
		try {
			String fileContent = filesystem.openFileString(dbFilePath);
			// AbstractTreeItem rootItem = SimpleTreeSerializer.loadTree(fileContent); // porting db to JSON
			AbstractTreeItem rootItem = persistenceService.deserializeTree(fileContent);
			treeManager.setRootItem(rootItem);
			logger.info("Database loaded.");
		} catch (IOException | DeserializationFailedException e) {
			changesHistory.registerChange();
			logger.error(e);
			userInfo.showInfo("Failed to load database: " + e.getMessage());
		}
	}
	
	private void saveRootTree() {
		PathBuilder dbFilePath = filesystem.externalAndroidDir()
				.append(preferences.getValue(PropertyDefinition.dbFilePath, String.class));
		try {
			String output = persistenceService.serializeTree(treeManager.getRootItem());
			//Logs.debug("Serialized data: " + output);
			filesystem.saveFile(dbFilePath.toString(), output);
		} catch (IOException e) {
			logger.error(e);
		}
		logger.debug("Database saved successfully.");
	}
	
	public void optionRestoreBackup() {
		new BackupListMenu().show();
	}
}
