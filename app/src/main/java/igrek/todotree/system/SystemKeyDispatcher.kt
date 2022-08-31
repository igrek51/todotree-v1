package igrek.todotree.system

import android.view.KeyEvent
import igrek.todotree.intent.NavigationCommand

class SystemKeyDispatcher {

    fun onKeyDown(keyCode: Int): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                return onKeyBack()
            }
            KeyEvent.KEYCODE_MENU -> {
                return onKeyMenu()
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
                return onVolumeUp()
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                return onVolumeDown()
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                return onArrowUp()
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                return onArrowDown()
            }
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_MEDIA_REWIND,
            KeyEvent.KEYCODE_MEDIA_PREVIOUS,
            KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD,
            KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD -> {
                return onArrowLeft()
            }
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
            KeyEvent.KEYCODE_MEDIA_NEXT,
            KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD,
            KeyEvent.KEYCODE_MEDIA_STEP_FORWARD -> {
                return onArrowRight()
            }
            KeyEvent.KEYCODE_HEADSETHOOK, // mini jack headset button
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_MEDIA_PLAY,
            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                return onMediaButton()
            }
        }
        return false
    }

    fun onKeyBack(): Boolean {
        return NavigationCommand().backClicked()
    }

    private fun onKeyMenu(): Boolean {
        return false
    }

    private fun onVolumeUp(): Boolean {
        return NavigationCommand().approveClicked()
    }

    private fun onVolumeDown(): Boolean {
        return NavigationCommand().approveClicked()
    }

    private fun onArrowUp(): Boolean {
        return false
    }

    private fun onArrowDown(): Boolean {
        return false
    }

    private fun onArrowLeft(): Boolean {
        return false
    }

    private fun onArrowRight(): Boolean {
        return false
    }

    private fun onMediaButton(): Boolean {
        return false
    }
}
