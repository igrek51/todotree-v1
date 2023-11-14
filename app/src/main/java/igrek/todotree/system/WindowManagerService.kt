package igrek.todotree.system

import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.appFactory

@Suppress("DEPRECATION")
class WindowManagerService {
    private val appCompatActivity: AppCompatActivity by LazyExtractor(appFactory.appCompatActivity)

    private val dpi: Int
        get() {
            val metrics = appCompatActivity.resources.displayMetrics
            return metrics.densityDpi
        }

    fun keepScreenOn(set: Boolean) {
        if (set) {
            appCompatActivity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            appCompatActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        setShowWhenLocked(set)
    }

    fun hideTaskbar() {
        appCompatActivity.supportActionBar?.hide()
    }

    fun setFullscreen(set: Boolean) {
        val flag = WindowManager.LayoutParams.FLAG_FULLSCREEN
        if (set) {
            appCompatActivity.window?.addFlags(flag)
        } else {
            appCompatActivity.window?.clearFlags(flag)
        }
    }

    private fun setShowWhenLocked(set: Boolean) {
        val flag =
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        if (set) {
            appCompatActivity.window?.setFlags(flag, flag)
        } else {
            appCompatActivity.window?.clearFlags(flag)
        }
    }

    fun dp2px(dp: Float): Float {
        return dp * (dpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun showAppWhenLocked() {
        appCompatActivity.window
            .addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
    }
}
