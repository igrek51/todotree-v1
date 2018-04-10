package igrek.todotree.services.statistics;

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

public class StatisticsLogService {
	
	private static final int KEEP_LOGS_DAYS = 14;
	private static final String LOGS_SUBDIR = "stats";
	private static final String LOG_FILENAME_PREFIX = "stats-";
	private static final String LOG_FILENAME_SUFFIX = ".log";
	private SimpleDateFormat filenameDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
	private SimpleDateFormat lineDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.ENGLISH);
	
	private FilesystemService filesystem;
	private Preferences preferences;
	private Logs logger;
	
	public StatisticsLogService(FilesystemService filesystem, Preferences preferences, Logs logger) {
		this.filesystem = filesystem;
		this.preferences = preferences;
		this.logger = logger;
	}
	
	/**
	 * @return latest log file
	 */
	private File getTodayLog() throws IOException {
		File logFile = getLogsDirPath().append(LOG_FILENAME_PREFIX + filenameDateFormat.format(new Date()) + LOG_FILENAME_SUFFIX)
				.getFile();
		if (!logFile.exists())
			logFile.createNewFile();
		return logFile;
	}

	public void logTaskCreate(String taskName) {
		logTaskChange(taskName, "task-create");
	}

	public void logTaskRemove(AbstractTreeItem item) {
		// TODO log item and its children
		// TODO if its not link
		logTaskRemove(item.getdisplayName());
		for (AbstractTreeItem child : item.children()){
			logTaskRemove(child);
		}
	}

	public void logTaskRemove(String taskName) {
		logTaskChange(taskName, "task-remove");
	}
	
	/**
	 * logs task event
	 */
	public void logTaskChange(String taskName, String type) {
		try {
			File todayLog = getTodayLog();
			StringBuilder line = new StringBuilder();
			line.append(type).append("\t");
			line.append(lineDateFormat.format(new Date()));
			line.append(taskName);
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
	 * removes old logs
	 */
	public void cleanUpLogs() {
		PathBuilder logsDirPath = getLogsDirPath();
		List<String> logs = filesystem.listDir(logsDirPath);
		// minimal keeping date = today minus keepDays
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DATE, -KEEP_LOGS_DAYS);
		Date minDate = c.getTime();
		
		for (String filename : logs) {
			if (filename.startsWith(LOG_FILENAME_PREFIX)) {
				String datePart = PathBuilder.removeExtension(filename)
						.substring(LOG_FILENAME_PREFIX.length());
				try {
					Date date = filenameDateFormat.parse(datePart);
					if (date.before(minDate)) {
						// need to be removed
						removeLog(logsDirPath, filename);
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
	
	private PathBuilder getLogsDirPath() {
		PathBuilder dirPath = getDBFilePath().parent().append(LOGS_SUBDIR);
		if (!dirPath.getFile().exists()) {
			dirPath.getFile().mkdir();
			logger.info("missing dir created: " + dirPath.toString());
		}
		return dirPath;
	}
	
	private PathBuilder getDBFilePath() {
		return filesystem.externalAndroidDir()
				.append(preferences.getValue(PropertyDefinition.dbFilePath, String.class));
	}
	
}