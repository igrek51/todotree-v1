package igrek.todotree.logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

public class Logs {
	
	private Logs() {}
	
	private static final LogLevel CONSOLE_LEVEL = LogLevel.TRACE;
	
	private static final String LOG_TAG = "ylog";
	private static final boolean SHOW_EXCEPTIONS_TRACE = true;
	private static final LogLevel SHOW_TRACE_DETAILS_LEVEL = LogLevel.TRACE;
	
	public static void error(String message) {
		log(message, LogLevel.ERROR, "[ERROR] ");
	}
	
	public static void error(Throwable ex) {
		log(ex.getMessage(), LogLevel.ERROR, "[EXCEPTION - " + ex.getClass().getName() + "] ");
		printExceptionStackTrace(ex);
	}
	
	public static void errorUncaught(Throwable ex) {
		log(ex.getMessage(), LogLevel.FATAL, "[UNCAUGHT EXCEPTION - " + ex.getClass()
				.getName() + "] ");
		printExceptionStackTrace(ex);
	}
	
	public static void fatal(final Activity activity, String e) {
		log(e, LogLevel.FATAL, "[FATAL ERROR] ");
		if (activity == null) {
			error("FATAL ERROR: Brak activity");
			return;
		}
		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(activity);
		dlgAlert.setMessage(e);
		dlgAlert.setTitle("Błąd krytyczny");
		dlgAlert.setPositiveButton("Zamknij aplikację", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				activity.finish();
			}
		});
		dlgAlert.setCancelable(false);
		dlgAlert.create().show();
	}
	
	public static void fatal(final Activity activity, Throwable ex) {
		String e = ex.getClass().getName() + " - " + ex.getMessage();
		printExceptionStackTrace(ex);
		fatal(activity, e);
	}
	
	public static void warn(String message) {
		log(message, LogLevel.WARN, "[warn] ");
	}
	
	public static void info(String message) {
		log(message, LogLevel.INFO, "");
	}
	
	public static void debug(String message) {
		log(message, LogLevel.DEBUG, "[debug] ");
	}
	
	public static void trace(String message) {
		log(message, LogLevel.TRACE, "[trace] ");
	}
	
	
	private static void log(String message, LogLevel level, String logPrefix) {
		
		if (level.lowerOrEqual(CONSOLE_LEVEL)) {
			
			String consoleMessage;
			if (level.higherOrEqual(SHOW_TRACE_DETAILS_LEVEL)) {
				final int stackTraceIndex = 4;
				
				StackTraceElement ste = Thread.currentThread().getStackTrace()[stackTraceIndex];
				
				String methodName = ste.getMethodName();
				String fileName = ste.getFileName();
				int lineNumber = ste.getLineNumber();
				
				consoleMessage = logPrefix + methodName + "(" + fileName + ":" + lineNumber + "): " + message;
			} else {
				consoleMessage = logPrefix + message;
			}
			
			if (level.lowerOrEqual(LogLevel.ERROR)) {
				Log.e(LOG_TAG, consoleMessage);
			} else {
				Log.i(LOG_TAG, consoleMessage);
			}
		}
	}
	
	public static void printStackTrace() {
		int i = 0;
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
			i++;
			if (i <= 3)
				continue;
			String methodName = ste.getMethodName();
			String fileName = ste.getFileName();
			int lineNumber = ste.getLineNumber();
			String consoleMessage = "[trace] STACK TRACE " + (i - 3) + ": " + methodName + "(" + fileName + ":" + lineNumber + ")";
			Log.i(LOG_TAG, consoleMessage);
		}
	}
	
	private static void printExceptionStackTrace(Throwable ex) {
		if (SHOW_EXCEPTIONS_TRACE) {
			Log.e(LOG_TAG, Log.getStackTraceString(ex));
		}
	}
	
	public static void trace() {
		log("Quick Trace: " + System.currentTimeMillis(), LogLevel.DEBUG, "[trace] ");
	}
}
