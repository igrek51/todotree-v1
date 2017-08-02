package igrek.todotree.controller;


import java.io.IOException;
import java.text.ParseException;

import javax.inject.Inject;

import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.datatree.TreeManager;
import igrek.todotree.datatree.TreeScrollCache;
import igrek.todotree.datatree.item.TreeItem;
import igrek.todotree.datatree.serializer.TreeSerializer;
import igrek.todotree.logger.Logs;
import igrek.todotree.services.backup.BackupManager;
import igrek.todotree.services.filesystem.FilesystemService;
import igrek.todotree.services.filesystem.PathBuilder;
import igrek.todotree.services.history.ChangesHistory;
import igrek.todotree.services.preferences.Preferences;
import igrek.todotree.services.resources.UserInfoService;

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
	TreeSerializer treeSerializer;
	
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
			TreeItem rootItem = treeSerializer.loadTree(fileContent);
			treeManager.setRootItem(rootItem);
			Logs.info("Database loaded.");
		} catch (IOException | ParseException e) {
			Logs.error(e);
		}
	}
	
	private void saveRootTree() {
		PathBuilder dbFilePath = filesystem.pathSD().append(preferences.dbFilePath);
		try {
			String output = treeSerializer.saveTree(treeManager.getRootItem());
			filesystem.saveFile(dbFilePath.toString(), output);
		} catch (IOException e) {
			Logs.error(e);
		}
		Logs.debug("Database saved successfully.");
	}
}
