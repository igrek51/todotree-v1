package igrek.todotree.mock;


import android.app.Activity;
import android.util.Log;

import igrek.todotree.logger.LogLevel;
import igrek.todotree.logger.Logs;

public class MockedLogs extends Logs {
	
	@Override
	public void fatal(Activity activity, String e) {
		log(e, LogLevel.FATAL, "[FATAL ERROR] ");
	}
	
	@Override
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
				System.err.println(consoleMessage);
			} else {
				System.out.println(consoleMessage);
			}
		}
	}
	
	@Override
	protected void printExceptionStackTrace(Throwable ex) {
		if (SHOW_EXCEPTIONS_TRACE) {
			System.err.println(Log.getStackTraceString(ex));
		}
	}
	
	@Override
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
			System.out.println(consoleMessage);
		}
	}
}
