package igrek.todotree.service.summary;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import igrek.todotree.R;
import igrek.todotree.activity.MainActivity;
import igrek.todotree.info.logger.Logger;
import igrek.todotree.info.logger.LoggerFactory;

public class NotificationService {
	
	private static final String CHANNEL_ID = "todoTreeChanellNo5";
	private Logger logger = LoggerFactory.INSTANCE.getLogger();
	
	public NotificationService() {
	}
	
	public void sendNotification(Context context, String title, String text) {
		
		logger.debug("creating notification");
		
		Intent intent = new Intent(context, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.icon_launcher)
				.setContentTitle(title)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(text))
				.setContentText(text)
				.setContentIntent(pendingIntent)
				.setPriority(NotificationCompat.PRIORITY_DEFAULT)
				.setAutoCancel(true);
		
		createNotificationChannel(context);
		
		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
		
		// notificationId is a unique int for each notification that you must define
		int notificationId = 7;
		notificationManager.notify(notificationId, mBuilder.build());
	}
	
	private void createNotificationChannel(Context context) {
		// Create the NotificationChannel, but only on API 26+ because
		// the NotificationChannel class is new and not in the support library
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			String description = "dupa channel description";
			int importance = NotificationManager.IMPORTANCE_DEFAULT;
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance);
			channel.setDescription(description);
			// Register the channel with the system; you can't change the importance
			// or other notification behaviors after this
			NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
			if (notificationManager != null) {
				notificationManager.createNotificationChannel(channel);
			}
		}
	}
}
