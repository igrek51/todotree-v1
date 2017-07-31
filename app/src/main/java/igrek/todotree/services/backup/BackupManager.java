package igrek.todotree.services.backup;

import android.util.Pair;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import igrek.todotree.logger.Logs;
import igrek.todotree.services.filesystem.FilesystemService;
import igrek.todotree.services.filesystem.PathBuilder;
import igrek.todotree.services.preferences.Preferences;

public class BackupManager {
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy-HH_mm_ss", Locale.ENGLISH);
	
	public static final String BACKUP_FILE_PREFIX = "backup_";
	
	/** backupy z ostatnio zapisanych baz */
	public static final int BACKUP_LAST_VERSIONS = 10;
	/** backupy z ostatnich dni */
	public static final int BACKUP_LAST_DAYS = 14;
	
	private FilesystemService filesystem;
	private Preferences preferences;
	
	public BackupManager(Preferences preferences, FilesystemService filesystem) {
		this.preferences = preferences;
		this.filesystem = filesystem;
	}
	
	public void saveBackupFile() {
		
		if (BACKUP_LAST_VERSIONS == 0)
			return;
		
		PathBuilder dbFilePath = filesystem.pathSD().append(preferences.dbFilePath);
		PathBuilder dbDirPath = dbFilePath.parent();
		
		//zapisanie nowego backupa
		saveNewBackup(dbDirPath, dbFilePath);
		
		//usunięcie starych backupów
		removeOldBackups(dbDirPath);
	}
	
	private void saveNewBackup(PathBuilder dbDirPath, PathBuilder dbFilePath) {
		PathBuilder backupPath = dbDirPath.append(BACKUP_FILE_PREFIX + dateFormat.format(new Date()));
		try {
			filesystem.copy(new File(dbFilePath.toString()), new File(backupPath.toString()));
			Logs.info("Utworzono backup: " + backupPath.toString());
		} catch (IOException e) {
			Logs.error(e);
		}
	}
	
	private void removeOldBackups(PathBuilder dbDirPath) {
		
		List<String> children = filesystem.listDir(dbDirPath);
		
		List<Pair<String, Date>> backups = new ArrayList<>();
		//rozpoznanie plików backup i odczytanie ich dat
		for (String child : children) {
			if (child.startsWith(BACKUP_FILE_PREFIX)) {
				String dateStr = PathBuilder.removeExtension(child)
						.substring(BACKUP_FILE_PREFIX.length());
				Date date = null;
				try {
					date = dateFormat.parse(dateStr);
				} catch (ParseException e) {
					Logs.warn("Niepoprawny format daty w nazwie pliku: " + child);
				}
				backups.add(new Pair<>(child, date));
			}
		}
		
		//posortowanie po datach malejąco (od najnowszych)
		Collections.sort(backups, new Comparator<Pair<String, Date>>() {
			@Override
			public int compare(Pair<String, Date> a, Pair<String, Date> b) {
				if (a.second == null)
					return +1;
				if (b.second == null)
					return -1;
				return -a.second.compareTo(b.second);
			}
		});
		
		//pozostawienie kilku najnowszych backupów
		for (int i = 0; i < BACKUP_LAST_VERSIONS && !backups.isEmpty(); i++) {
			backups.remove(0);
		}
		
		//pozostawienie backupów z różnych dni i ograniczonej liczbie dni wstecz
		if (BACKUP_LAST_DAYS > 0) {
			for (int i = 1; i <= BACKUP_LAST_DAYS; i++) {
				// dzień, z którego należy nie usuwać najwyżej jednego backupa
				Calendar calendar1 = Calendar.getInstance();
				calendar1.setTime(new Date());
				calendar1.add(Calendar.DAY_OF_MONTH, -i);
				//poszukiwanie backupa z tego dnia
				for (int j = 0; j < backups.size(); j++) {
					Pair<String, Date> backup = backups.get(j);
					if (sameDay(calendar1, backup.second)) {
						backups.remove(j); //zachowaj ten backup
						break;
					}
				}
			}
		}
		
		//usunięcie pozostałych plików
		for (Pair<String, Date> pair : backups) {
			PathBuilder toRemovePath = dbDirPath.append(pair.first);
			filesystem.delete(toRemovePath);
			Logs.info("Usunięto stary backup: " + toRemovePath.toString());
		}
		
	}
	
	private boolean sameDay(Calendar cal1, Date date2) {
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2
				.get(Calendar.DAY_OF_YEAR);
	}
}
