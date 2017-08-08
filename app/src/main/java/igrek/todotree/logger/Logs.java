package igrek.todotree.logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

public class Logs {
	
	public Logs() {
	}
	
	protected static final LogLevel CONSOLE_LEVEL = LogLevel.TRACE;
	
	private static final String LOG_TAG = "ylog";
	protected static final boolean SHOW_EXCEPTIONS_TRACE = true;
	protected static final LogLevel SHOW_TRACE_DETAILS_LEVEL = LogLevel.TRACE;
	
	public void error(String message) {
		log(message, LogLevel.ERROR, "[ERROR] ");
	}
	
	public void error(Throwable ex) {
		log(ex.getMessage(), LogLevel.ERROR, "[EXCEPTION - " + ex.getClass().getName() + "] ");
		printExceptionStackTrace(ex);
	}
	
	public void errorUncaught(Throwable ex) {
		log(ex.getMessage(), LogLevel.FATAL, "[UNCAUGHT EXCEPTION - " + ex.getClass()
				.getName() + "] ");
		printExceptionStackTrace(ex);
	}
	
	public void fatal(final Activity activity, String e) {
		log(e, LogLevel.FATAL, "[FATAL ERROR] ");
		if (activity == null) {
			error("FATAL ERROR: No activity");
			return;
		}
		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(activity);
		dlgAlert.setMessage(e);
		dlgAlert.setTitle("Critical error");
		dlgAlert.setPositiveButton("Close app", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				activity.finish();
			}
		});
		dlgAlert.setCancelable(false);
		dlgAlert.create().show();
	}
	
	public void fatal(final Activity activity, Throwable ex) {
		String e = ex.getClass().getName() + " - " + ex.getMessage();
		printExceptionStackTrace(ex);
		fatal(activity, e);
	}
	
	public void warn(String message) {
		log(message, LogLevel.WARN, "[warn] ");
	}
	
	public void info(String message) {
		log(message, LogLevel.INFO, "");
	}
	
	public void debug(String message) {
		log(message, LogLevel.DEBUG, "[debug] ");
	}
	
	public void trace(String message) {
		log(message, LogLevel.TRACE, "[trace] ");
	}
	
	
	protected void log(String message, LogLevel level, String logPrefix) {
		
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
	
	public void printStackTrace() {
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
	
	protected void printExceptionStackTrace(Throwable ex) {
		if (SHOW_EXCEPTIONS_TRACE) {
			Log.e(LOG_TAG, Log.getStackTraceString(ex));
		}
	}
	
	public void trace() {
		log("Quick Trace: " + System.currentTimeMillis(), LogLevel.DEBUG, "[trace] ");
	}
}
