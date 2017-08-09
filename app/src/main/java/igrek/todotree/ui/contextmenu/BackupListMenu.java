package igrek.todotree.ui.contextmenu;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import igrek.todotree.controller.GUIController;
import igrek.todotree.controller.PersistenceController;
import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.services.backup.Backup;
import igrek.todotree.services.backup.BackupManager;
import igrek.todotree.services.resources.UserInfoService;
import igrek.todotree.services.tree.TreeManager;
import igrek.todotree.services.tree.TreeScrollCache;
import igrek.todotree.ui.errorcheck.UIErrorHandler;

public class BackupListMenu {
	
	@Inject
	Activity activity;
	
	@Inject
	BackupManager backupManager;
	
	@Inject
	UserInfoService userInfo;
	
	@Inject
	TreeScrollCache scrollCache;
	
	@Inject
	TreeManager treeManager;
	
	private SimpleDateFormat displayDateFormat = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss", Locale.ENGLISH);
	
	public BackupListMenu() {
		DaggerIOC.getAppComponent().inject(this);
	}
	
	public void show() {
		
		final List<RestoreBackupAction> actions = buildActionsList();
		CharSequence[] actionNames = convertToNamesArray(actions);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Choose backup");
		builder.setItems(actionNames, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				try {
					actions.get(item).execute();
				} catch (Throwable t) {
					UIErrorHandler.showError(t);
				}
			}
		});
		
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private List<RestoreBackupAction> buildActionsList() {
		List<RestoreBackupAction> actions = new ArrayList<>();
		
		List<Backup> backups = backupManager.getBackups();
		
		for (final Backup backup : backups) {
			actions.add(new RestoreBackupAction(displayDateFormat.format(backup.getDate())) {
				@Override
				public void execute() {
					treeManager.reset();
					scrollCache.clear();
					new PersistenceController().loadRootTreeFromBackup(backup);
					new GUIController().updateItemsList();
					userInfo.showInfo("Database backup loaded: " + backup.getFilename());
				}
			});
		}
		
		return actions;
	}
	
	private CharSequence[] convertToNamesArray(List<RestoreBackupAction> actions) {
		CharSequence[] actionNames = new CharSequence[actions.size()];
		for (int i = 0; i < actions.size(); i++) {
			actionNames[i] = actions.get(i).getName();
		}
		return actionNames;
	}
}
