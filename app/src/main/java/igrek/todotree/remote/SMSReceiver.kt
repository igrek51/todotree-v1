package igrek.todotree.remote

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import igrek.todotree.info.logger.LoggerFactory

class SMSReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val extras = intent.extras

        LoggerFactory.logger.debug("SMS received")

        if (extras != null) {
            val pdus = extras["pdus"] as Array<ByteArray>?
            for (i in 0 until (pdus?.size ?: 0)) {
                val message: SmsMessage = SmsMessage.createFromPdu(pdus!![i])

                val from = message.originatingAddress
                val body = message.messageBody

                LoggerFactory.logger.debug("SMS received, from: ${from}, body: $body")

                val simplified = body.lowercase().trim()
                if (simplified.startsWith("sudo ")) {
                    LoggerFactory.logger.info("SUDO prefix detected, running remote command")
                    val cmd = simplified.removePrefix("sudo ")
                    RemoteCommander(context).commandAttempt(cmd)
                }
            }
        }
    }
}