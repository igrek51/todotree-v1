package igrek.todotree.services.backup;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import igrek.todotree.logger.Logs;
import igrek.todotree.services.filesystem.FilesystemService;
import igrek.todotree.services.filesystem.PathBuilder;
import igrek.todotree.services.preferences.Preferences;
import igrek.todotree.services.preferences.PropertyDefinition;

public class BackupManager {
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss", Locale.ENGLISH);
	
	private static final String BACKUP_FILE_PREFIX = "backup_";
	
	/** max count of last versions backups */
	private static final int BACKUP_LAST_VERSIONS = 10;
	/** daily backups count */
	private static final int BACKUP_LAST_DAYS = 14;
	
	private FilesystemService filesystem;
	private Preferences preferences;
	private Logs logger;
	
	public BackupManager(Preferences preferences, FilesystemService filesystem, Logs logger) {
		this.preferences = preferences;
		this.filesystem = filesystem;
		this.logger = logger;
	}
	
	public void saveBackupFile() {
		
		if (BACKUP_LAST_VERSIONS == 0)
			return;
		
		PathBuilder dbFilePath = getDBFilePath();
		PathBuilder dbDirPath = dbFilePath.parent();
		
		saveNewBackup(dbDirPath, dbFilePath);
		
		removeOldBackups(dbDirPath);
	}
	
	private void saveNewBackup(PathBuilder dbDirPath, PathBuilder dbFilePath) {
		PathBuilder backupPath = dbDirPath.append(BACKUP_FILE_PREFIX + dateFormat.format(new Date()));
		try {
			filesystem.copy(new File(dbFilePath.toString()), new File(backupPath.toString()));
			logger.info("Backup created: " + backupPath.toString());
		} catch (IOException e) {
			logger.error(e);
		}
	}
	
	private void removeOldBackups(PathBuilder dbDirPath) {
		
		// backup files list to remove
		List<Backup> backups = getBackups();
		
		// retain few newest backups
		for (int i = 0; i < BACKUP_LAST_VERSIONS && !backups.isEmpty(); i++) {
			backups.remove(0);
		}
		
		// retain backups from different days
		if (BACKUP_LAST_DAYS > 0) {
			for (int i = 1; i <= BACKUP_LAST_DAYS; i++) {
				// calculate days before
				Calendar calendar1 = Calendar.getInstance();
				calendar1.setTime(new Date());
				calendar1.add(Calendar.DAY_OF_MONTH, -i);
				// find newest backup from that day
				for (int j = 0; j < backups.size(); j++) {
					Backup backup = backups.get(j);
					if (isSameDay(calendar1, backup.getDate())) {
						backups.remove(j); // and retain this backup
						break;
					}
				}
			}
		}
		
		// remove other backups
		for (Backup backup : backups) {
			PathBuilder toRemovePath = dbDirPath.append(backup.getFilename());
			filesystem.delete(toRemovePath);
			logger.info("Old backup has been removed: " + toRemovePath.toString());
		}
		
	}
	
	@NonNull
	public List<Backup> getBackups() {
		
		PathBuilder dbDirPath = getDBFilePath().parent();
		
		List<String> children = filesystem.listDir(dbDirPath);
		
		List<Backup> backups = new ArrayList<>();
		// recognize bbackup files and read date from name
		for (String filename : children) {
			if (filename.startsWith(BACKUP_FILE_PREFIX)) {
				String dateStr = PathBuilder.removeExtension(filename)
						.substring(BACKUP_FILE_PREFIX.length());
				Date date = null;
				try {
					date = dateFormat.parse(dateStr);
				} catch (ParseException e) {
					logger.warn("Invalid date format in file name: " + filename);
				}
				backups.add(new Backup(filename, date));
			}
		}
		
		Collections.sort(backups);
		
		return backups;
	}
	
	private PathBuilder getDBFilePath() {
		return filesystem.externalAndroidDir()
				.append(preferences.getValue(PropertyDefinition.dbFilePath, String.class));
	}
	
	private boolean isSameDay(Calendar cal1, Date date2) {
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2
				.get(Calendar.DAY_OF_YEAR);
	}
}
