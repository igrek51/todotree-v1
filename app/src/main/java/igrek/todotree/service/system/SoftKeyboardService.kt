package igrek.todotree.service.system

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.appFactory


class SoftKeyboardService {
    private val appCompatActivity: AppCompatActivity by LazyExtractor(appFactory.appCompatActivity)

    private val imm: InputMethodManager? = appCompatActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    private val logger = LoggerFactory.logger

    private fun hideSoftKeyboard(view: View?) {
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
        var view: View? = appCompatActivity.currentFocus
        if (view == null) {
            view = View(appCompatActivity)
        }
        hideSoftKeyboard(view)
    }

    fun showSoftKeyboard(view: View? = null) {
        var mView: View? = view
        if (mView == null) {
            mView = appCompatActivity.currentFocus
        }
        if (mView == null) {
            mView = View(appCompatActivity)
        }
        imm?.showSoftInput(mView, 0)
    }
}
