package igrek.todotree.service.system

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory


class SoftKeyboardService(
    private val appCompatActivity: LazyInject<AppCompatActivity?> = appFactory.appCompatActivity,
) {

    private val imm: InputMethodManager? = appCompatActivity.get()!!.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    private val logger = LoggerFactory.logger

    fun hideSoftKeyboard(view: View?) {
        if (imm == null) {
            logger.error("no input method manager")
            return
        }
        if (view == null) {
            logger.error("view = null")
            return
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun hideSoftKeyboard() {
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = appCompatActivity.get()!!.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(appCompatActivity.get()!!)
        }
        hideSoftKeyboard(view)
    }

    fun showSoftKeyboard(view: View?) {
        imm?.showSoftInput(view, 0)
    }
}
