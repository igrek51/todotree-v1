package igrek.todotree.controller;


import java.io.IOException;

import javax.inject.Inject;

import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.exceptions.DeserializationFailedException;
import igrek.todotree.logger.Logs;
import igrek.todotree.model.treeitem.AbstractTreeItem;
import igrek.todotree.services.backup.BackupManager;
import igrek.todotree.services.filesystem.FilesystemService;
import igrek.todotree.services.filesystem.PathBuilder;
import igrek.todotree.services.history.ChangesHistory;
import igrek.todotree.services.preferences.Preferences;
import igrek.todotree.services.resources.UserInfoService;
import igrek.todotree.services.tree.TreeManager;
import igrek.todotree.services.tree.TreeScrollCache;
import igrek.todotree.services.tree.serializer.JsonTreeSerializer;

public class PersistenceController {
	
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
	JsonTreeSerializer treeSerializer;
	
	@Inject
	ChangesHistory changesHistory;
	
	public PersistenceController() {
		DaggerIOC.getAppComponent().inject(this);
	}
	
	void optionReload() {
		treeManager.reset();
		scrollCache.clear();
		loadRootTree();
		new GUIController().updateItemsList();
		userInfo.showInfo("Database loaded.");
	}
	
	void optionSave() {
		saveDatabase();
		userInfo.showInfo("Database saved.");
	}
	
	void saveDatabase() {
		if (!changesHistory.hasChanges()) {
			Logs.info("No changes have been made - skipping saving");
			return;
		}
		
		saveRootTree();
		backupManager.saveBackupFile();
	}
	
	public void loadRootTree() {
		changesHistory.clear();
		filesystem.mkdirIfNotExist(filesystem.pathSD().toString());
		PathBuilder dbFilePath = filesystem.pathSD().append(preferences.dbFilePath);
		Logs.info("Loading database from file: " + dbFilePath.toString());
		if (!filesystem.exists(dbFilePath.toString())) {
			Logs.warn("Database file does not exist. Default empty database created.");
			return;
		}
		try {
			String fileContent = filesystem.openFileString(dbFilePath.toString());
			// AbstractTreeItem rootItem = SimpleTreeSerializer.loadTree(fileContent); // porting db to JSON
			AbstractTreeItem rootItem = treeSerializer.deserializeTree(fileContent);
			treeManager.setRootItem(rootItem);
			Logs.info("Database loaded.");
		} catch (IOException | DeserializationFailedException e) {
			Logs.error(e);
		}
	}
	
	private void saveRootTree() {
		PathBuilder dbFilePath = filesystem.pathSD().append(preferences.dbFilePath);
		try {
			String output = treeSerializer.serializeTree(treeManager.getRootItem());
			//Logs.debug("Serialized data: " + output);
			filesystem.saveFile(dbFilePath.toString(), output);
		} catch (IOException e) {
			Logs.error(e);
		}
		Logs.debug("Database saved successfully.");
	}
}
