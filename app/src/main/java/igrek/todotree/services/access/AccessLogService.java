package igrek.todotree.services.access;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import igrek.todotree.logger.Logs;
import igrek.todotree.model.treeitem.AbstractTreeItem;
import igrek.todotree.services.filesystem.FilesystemService;
import igrek.todotree.services.filesystem.PathBuilder;
import igrek.todotree.services.preferences.Preferences;
import igrek.todotree.services.preferences.PropertyDefinition;

public class AccessLogService {
	
	private static final int ACCESS_LOGS_DAYS = 14;
	private static final String ACCESS_LOGS_SUBDIR = "access";
	private static final String ACCESS_LOG_PREFIX = "access-";
	private static final String ACCESS_LOG_SUFFIX = ".log";
	private SimpleDateFormat filenameDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
	private SimpleDateFormat lineDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.ENGLISH);
	
	private FilesystemService filesystem;
	private Preferences preferences;
	private Logs logger;
	
	public AccessLogService(FilesystemService filesystem, Preferences preferences, Logs logger) {
		this.filesystem = filesystem;
		this.preferences = preferences;
		this.logger = logger;
	}
	
	/**
	 * @return current access log file
	 */
	private File getTodayLog() throws IOException {
		File logFile = getAccessDirPath().append(ACCESS_LOG_PREFIX + filenameDateFormat.format(new Date()) + ACCESS_LOG_SUFFIX)
				.getFile();
		if (!logFile.exists())
			logFile.createNewFile();
		return logFile;
	}
	
	/**
	 * logs database unlock event
	 */
	public void logDBUnlocked(AbstractTreeItem item) {
		try {
			File todayLog = getTodayLog();
			StringBuilder line = new StringBuilder();
			line.append("db-unlocked\t");
			line.append(lineDateFormat.format(new Date()));
			if (item != null)
				line.append("\t" + item.getDisplayName());
			appendLine(todayLog, line.toString());
			cleanUpLogs();
		} catch (IOException e) {
			logger.error(e);
		}
	}
	
	private void appendLine(File file, String line) throws IOException {
		try (FileWriter fw = new FileWriter(file, true)) { // true = append file
			try (BufferedWriter bw = new BufferedWriter(fw)) {
				bw.write(line + "\n");
			}
		}
	}
	
	/**
	 * removes old access logs
	 */
	public void cleanUpLogs() {
		PathBuilder accessDirPath = getAccessDirPath();
		List<String> logs = filesystem.listDir(accessDirPath);
		// minimal keeping date = today minus keepDays
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DATE, -ACCESS_LOGS_DAYS);
		Date minDate = c.getTime();
		
		for (String filename : logs) {
			if (filename.startsWith(ACCESS_LOG_PREFIX)) {
				String datePart = PathBuilder.removeExtension(filename)
						.substring(ACCESS_LOG_PREFIX.length());
				try {
					Date date = filenameDateFormat.parse(datePart);
					if (date.before(minDate)) {
						// need to be removed
						removeLog(accessDirPath, filename);
					}
				} catch (ParseException e) {
					logger.warn("Invalid date format in file name: " + filename);
				}
			}
		}
	}
	
	private void removeLog(PathBuilder path, String filename) {
		path.append(filename).getFile().delete();
	}
	
	private PathBuilder getAccessDirPath() {
		PathBuilder dirPath = getDBFilePath().parent().append(ACCESS_LOGS_SUBDIR);
		if (!dirPath.getFile().exists()) {
			dirPath.getFile().mkdir();
			logger.info("missing access dir created: " + dirPath.toString());
		}
		return dirPath;
	}
	
	private PathBuilder getDBFilePath() {
		return filesystem.externalSDPath()
				.append(preferences.getValue(PropertyDefinition.dbFilePath, String.class));
	}
	
}
