package igrek.todotree.service.summary

import igrek.todotree.info.logger.LoggerFactory.logger
import igrek.todotree.info.logger.Logger.debug
import android.app.Activity
import android.app.AlarmManager
import igrek.todotree.info.logger.LoggerFactory
import org.joda.time.DateTime
import android.content.Intent
import igrek.todotree.activity.DailyReceiver
import android.app.PendingIntent
import android.content.Context
import android.os.Build

class AlarmService(private val activity: Activity) {
    private val alarmManager: AlarmManager
    private val logger = LoggerFactory.logger
    fun setAlarmAt(triggerTime: DateTime) {
        logger.debug("setting alarm at " + triggerTime.toString("HH:mm:ss, dd.MM.yyyy"))
        val intent = Intent(activity.applicationContext, DailyReceiver::class.java)
        intent.addCategory("android.intent.category.DEFAULT")
        val millis = triggerTime.millis
        val id = millis.toInt() // unique to enable multiple alarms
        val pendingIntent = PendingIntent.getBroadcast(
            activity.applicationContext,
            id,
            intent,
            PendingIntent.FLAG_ONE_SHOT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis, pendingIntent)
        } else {
            alarmManager[AlarmManager.RTC_WAKEUP, millis] = pendingIntent
        }
    }

    val nextMidnight: DateTime
        get() {
            val tomorrow = DateTime.now().plusDays(1)
            return tomorrow.withTimeAtStartOfDay()
        }

    init {
        alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }
}