package igrek.todotree.system

import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory

class WindowManagerService(
    private val appCompatActivity: LazyInject<AppCompatActivity?> = appFactory.appCompatActivity,
) {

    private val dpi: Int
        get() {
            val metrics = appCompatActivity.get()!!.resources.displayMetrics
            return metrics.densityDpi
        }

    fun keepScreenOn(set: Boolean) {
        if (set) {
            appCompatActivity.get()!!.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            appCompatActivity.get()!!.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        setShowWhenLocked(set)
    }

    fun hideTaskbar() {
        appCompatActivity.get()?.supportActionBar?.hide()
    }

    fun setFullscreen(set: Boolean) {
        val flag = WindowManager.LayoutParams.FLAG_FULLSCREEN
        if (set) {
            appCompatActivity.get()?.window?.addFlags(flag)
        } else {
            appCompatActivity.get()?.window?.clearFlags(flag)
        }
    }

    private fun setShowWhenLocked(set: Boolean) {
        val flag =
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        if (set) {
            appCompatActivity.get()?.window?.setFlags(flag, flag)
        } else {
            appCompatActivity.get()?.window?.clearFlags(flag)
        }
    }

    fun dp2px(dp: Float): Float {
        return dp * (dpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun showAppWhenLocked() {
        appCompatActivity.get()!!.window
            .addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
    }
}
