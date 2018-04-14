package igrek.todotree.services.statistics;

import com.google.common.io.Files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import igrek.todotree.domain.stats.StatisticEvent;
import igrek.todotree.domain.stats.StatisticEventType;
import igrek.todotree.logger.Logs;
import igrek.todotree.services.filesystem.FilesystemService;
import igrek.todotree.services.filesystem.PathBuilder;
import igrek.todotree.services.preferences.Preferences;
import igrek.todotree.services.preferences.PropertyDefinition;

public class StatisticsLogService {
	
	private static final int KEEP_LOGS_DAYS = 14;
	private static final String LOGS_SUBDIR = "stats";
	private static final String LOG_FILENAME_PREFIX = "stats-";
	private static final String LOG_FILENAME_SUFFIX = ".log";
	private static final SimpleDateFormat filenameDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
	private static final SimpleDateFormat lineDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.ENGLISH);
	
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
		File logFile = getLogFile(new Date());
		if (!logFile.exists())
			logFile.createNewFile();
		return logFile;
	}
	
	private File getLogFile(Date date) {
		return getLogsDirPath().append(LOG_FILENAME_PREFIX + filenameDateFormat.format(date) + LOG_FILENAME_SUFFIX)
				.getFile();
	}
	
	public void logTaskCreate(String taskName) {
		logTaskChange(taskName, StatisticEventType.TASK_CREATED);
	}
	
	public void logTaskComplete(String taskName) {
		logTaskChange(taskName, StatisticEventType.TASK_COMPLETED);
	}
	
	/**
	 * logs task event
	 */
	public void logTaskChange(String taskName, StatisticEventType type) {
		try {
			File todayLog = getTodayLog();
			StringBuilder line = new StringBuilder();
			line.append(type.getName()).append("\t");
			line.append(lineDateFormat.format(new Date())).append("\t");
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
		logger.debug("stats log line appended: " + line);
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
		logger.debug("log file removed: " + filename);
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
	
	public List<StatisticEvent> getLast24hEvents() throws Exception {
		Date today = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(today);
		c.add(Calendar.DATE, -1);
		Date yesterday = c.getTime();
		List<StatisticEvent> todayEvents = readEventsOfDay(today);
		List<StatisticEvent> yesterdayEvents = readEventsOfDay(yesterday);
		List<StatisticEvent> events = new ArrayList<>();
		for (StatisticEvent event : yesterdayEvents) {
			// is in 24h range from now
			if (event.getDatetime().after(yesterday)) {
				events.add(event);
			}
		}
		for (StatisticEvent event : todayEvents) {
			// is in 24h range from now
			if (event.getDatetime().after(yesterday)) {
				events.add(event);
			}
		}
		return events;
	}
	
	public List<StatisticEvent> readEventsOfDay(Date date) throws Exception {
		List<StatisticEvent> events = new ArrayList<>();
		File logFile = getLogFile(date);
		if (!logFile.exists()) {
			return events;
		}
		List<String> lines = Files.readLines(logFile, Charset.defaultCharset());
		for (String line : lines) {
			events.add(StatisticEvent.parse(line, lineDateFormat));
		}
		return events;
		// maybe some beautiful day Java 8 would be fully available on Android :(
		//		return lines.stream().map(String line -> StatisticEvent.parse(line)).collect(Collectors.toList());
	}
	
	
	
	
}
