package igrek.todotree.service.summary

import igrek.todotree.info.logger.LoggerFactory.logger
import igrek.todotree.info.logger.Logger.debug
import igrek.todotree.info.logger.LoggerFactory
import android.content.Intent
import igrek.todotree.activity.MainActivity
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import igrek.todotree.service.summary.NotificationService
import igrek.todotree.R
import androidx.core.app.NotificationManagerCompat
import android.app.NotificationManager
import android.app.NotificationChannel
import android.content.Context
import android.os.Build

class NotificationService {
    private val logger = LoggerFactory.logger
    fun sendNotification(context: Context, title: String?, text: String?) {
        logger.debug("creating notification")
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        val mBuilder =
            NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.icon_launcher)
                .setContentTitle(title)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
        createNotificationChannel(context)
        val notificationManager = NotificationManagerCompat.from(context)

        // notificationId is a unique int for each notification that you must define
        val notificationId = 7
        notificationManager.notify(notificationId, mBuilder.build())
    }

    private fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val description = "dupa channel description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = context.getSystemService(
                NotificationManager::class.java
            )
            notificationManager?.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "todoTreeChanellNo5"
    }
}