package igrek.todotree.controller;


import javax.inject.Inject;

import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.datatree.TreeManager;
import igrek.todotree.services.backup.BackupManager;
import igrek.todotree.services.resources.UserInfoService;

public class PersistenceController {
	
	@Inject
	TreeManager treeManager;
	
	@Inject
	UserInfoService userInfo;
	
	@Inject
	BackupManager backupManager;
	
	public PersistenceController() {
		DaggerIOC.getAppComponent().inject(this);
	}
	
	public void optionReload() {
		treeManager.reset();
		treeManager.loadRootTree();
		new GUIController().updateItemsList();
		userInfo.showInfo("Database loaded.");
	}
	
	public void optionSave() {
		saveDatabase();
		userInfo.showInfo("Database saved.");
	}
	
	public void saveDatabase() {
		treeManager.saveRootTree();
		backupManager.saveBackupFile();
	}
}
