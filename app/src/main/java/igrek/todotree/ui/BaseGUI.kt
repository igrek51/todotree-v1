package igrek.todotree.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity

abstract class BaseGUI internal constructor(
    protected var activity: AppCompatActivity?,
) {

    private val imm: InputMethodManager?
    var mainContent: RelativeLayout? = null

    fun setMainContentLayout(layoutResource: Int): View {
        mainContent?.removeAllViews()
        val inflater = activity!!.layoutInflater
        val layout = inflater.inflate(layoutResource, null)
        layout.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        mainContent?.addView(layout)
        return layout
    }

    fun hideSoftKeyboard(window: View) {
        imm?.hideSoftInputFromWindow(window.windowToken, 0)
    }

    fun showSoftKeyboard(window: View?) {
        imm?.showSoftInput(window, 0)
    }

    init {
        imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    }
}