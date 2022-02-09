package igrek.todotree.remote

import android.content.Context
import android.media.AudioManager
import android.widget.Toast
import igrek.todotree.info.logger.LoggerFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class VolumeManager (
    val context: Context,
) {

    private val logger = LoggerFactory.logger
    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun setSilentMode() {
        audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
        toast("Ringer set to SILENT")
    }

    fun setVibrateMode() {
        audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
        toast("Ringer set to VIBRATE")
    }

    fun unmute() {
        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        toast("Ringer set to NORMAL (unmuted)")
    }

    fun setMaxRingVolume() {
        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamMaxVolume(AudioManager.STREAM_RING), 0)
        toast("Ring volume set to maximum")
    }

    fun setMinRingVolume() {
        audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamMinVolume(AudioManager.STREAM_RING), 0)
        toast("RING volume set to minimum")
    }

    private fun toast(message: String) {
        logger.info("UI: toast: $message")
        GlobalScope.launch(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

}