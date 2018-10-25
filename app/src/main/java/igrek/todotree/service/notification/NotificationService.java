package igrek.todotree.service.notification;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import igrek.todotree.R;
import igrek.todotree.logger.Logger;
import igrek.todotree.logger.LoggerFactory;

public class NotificationService {
	
	private static final String CHANNEL_ID = "todoTreeChanellNo5";
	private Activity activity;
	private Logger logger = LoggerFactory.getLogger();
	
	public NotificationService(Activity activity) {
		this.activity = activity;
	}
	
	public void sendNotification() {
		
		logger.debug("creating notification");
		
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(activity, CHANNEL_ID).setSmallIcon(R.drawable.icon)
				.setContentTitle("Dupa")
				.setContentText("Hello Dupa!")
				.setPriority(NotificationCompat.PRIORITY_DEFAULT);
		
		createNotificationChannel();
		
		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(activity);
		
		// notificationId is a unique int for each notification that you must define
		int notificationId = 7;
		notificationManager.notify(notificationId, mBuilder.build());
	}
	
	private void createNotificationChannel() {
		// Create the NotificationChannel, but only on API 26+ because
		// the NotificationChannel class is new and not in the support library
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = CHANNEL_ID;
			String description = "dupa channel description";
			int importance = NotificationManager.IMPORTANCE_DEFAULT;
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
			channel.setDescription(description);
			// Register the channel with the system; you can't change the importance
			// or other notification behaviors after this
			NotificationManager notificationManager = activity.getSystemService(NotificationManager.class);
			notificationManager.createNotificationChannel(channel);
		}
	}
}
