package igrek.todotree.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import igrek.todotree.logger.Logger;
import igrek.todotree.logger.LoggerFactory;
import igrek.todotree.service.summary.DailySummaryService;

public class DailyReceiver extends BroadcastReceiver {
	
	private Logger logger = LoggerFactory.getLogger();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		logger.debug("received Daily Summary request");
		
		Intent newIntent = new Intent(context, MainActivity.class);
		Bundle b = new Bundle();
		b.putString(MainActivity.EXTRA_ACTION_KEY, DailySummaryService.DAILY_SUMMARY_ACTION);
		newIntent.putExtras(b);
		context.startActivity(newIntent);
		
	}
	
}