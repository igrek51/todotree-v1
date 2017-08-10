package igrek.todotree.mock;


import android.app.Activity;

import igrek.todotree.logger.LogLevel;
import igrek.todotree.logger.Logs;

public class MockedLogs extends Logs {
	
	@Override
	public void fatal(Activity activity, String e) {
		log(e, LogLevel.FATAL, "[FATAL ERROR] ");
	}
	
	@Override
	protected void printInfo(String msg) {
		System.out.println(msg);
	}
	
	@Override
	protected void printError(String msg) {
		System.err.println(msg);
	}
	
}
