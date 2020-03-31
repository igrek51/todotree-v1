package igrek.todotree.service.summary;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import org.joda.time.DateTime;

import igrek.todotree.activity.DailyReceiver;
import igrek.todotree.info.logger.Logger;
import igrek.todotree.info.logger.LoggerFactory;

public class AlarmService {
	
	private Activity activity;
	private AlarmManager alarmManager;
	private Logger logger = LoggerFactory.INSTANCE.getLogger();
	
	public AlarmService(Activity activity) {
		this.activity = activity;
		alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
	}
	
	public void setAlarmAt(DateTime triggerTime) {
		logger.debug("setting alarm at " + triggerTime.toString("HH:mm:ss, dd.MM.yyyy"));
		
		Intent intent = new Intent(activity.getApplicationContext(), DailyReceiver.class);
		intent.addCategory("android.intent.category.DEFAULT");
		long millis = triggerTime.getMillis();
		int id = (int) millis; // unique to enable multiple alarms
		
		PendingIntent pendingIntent = PendingIntent.getBroadcast(activity.getApplicationContext(), id, intent, PendingIntent.FLAG_ONE_SHOT);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis, pendingIntent);
		} else {
			alarmManager.set(AlarmManager.RTC_WAKEUP, millis, pendingIntent);
		}
	}
	
	public DateTime getNextMidnight() {
		DateTime tomorrow = DateTime.now().plusDays(1);
		return tomorrow.withTimeAtStartOfDay();
	}
}
