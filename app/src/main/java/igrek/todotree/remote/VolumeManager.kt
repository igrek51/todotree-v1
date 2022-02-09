package igrek.todotree.remote

import android.content.Context
import android.media.AudioManager
import igrek.todotree.info.logger.LoggerFactory

class VolumeManager (
    val context: Context,
) {

    private val logger = LoggerFactory.logger
    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun setSilentMode() {
        audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
        logger.info("Ringer mode set to SILENT")
    }

    fun setVibrateMode() {
        audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
        logger.info("Ringer mode set to VIBRATE")
    }

    fun unmute() {
        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        logger.info("Ringer mode set to NORMAL (unmuted)")
    }

    fun setMaxRingVolume() {
        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamMaxVolume(AudioManager.STREAM_RING), 0)
        logger.info("RING stream volume set to maximum")
    }

    fun setMinRingVolume() {
        audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamMinVolume(AudioManager.STREAM_RING), 0)
        logger.info("RING stream volume set to minimum")
    }

}