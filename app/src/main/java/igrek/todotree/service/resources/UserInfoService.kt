package igrek.todotree.service.resources

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import igrek.todotree.R
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.ui.GUI
import igrek.todotree.ui.errorcheck.SafeClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

open class UserInfoService(private val activity: Activity, private val gui: GUI) {
    private val mainView: View? = null
    private val infobars = HashMap<View?, Snackbar>()
    private val logger = LoggerFactory.logger

    private fun resString(resourceId: Int): String {
        return activity.resources.getString(resourceId)
    }

    /**
     * @param info       tekst do wyświetlenia lub zmiany
     * @param view       widok, na którym ma zostać wyświetlony tekst
     * @param actionName tekst przycisku akcji (jeśli null - brak przycisku akcji)
     * @param action     akcja kliknięcia przycisku (jeśli null - schowanie wyświetlanego tekstu)
     */
    private fun showActionInfo(info: String, view: View, actionName: String?, action: InfoBarClickAction?, color: Int?) {
        var view: View? = view
        var action = action
        if (view == null) {
            view = mainView
        }
        var snackbar = infobars[view]
        if (snackbar == null || !snackbar.isShown) { //nowy
            snackbar = Snackbar.make(view!!, info, Snackbar.LENGTH_SHORT)
            snackbar.setActionTextColor(Color.WHITE)
        } else { //widoczny - użyty kolejny raz
            snackbar.setText(info)
        }
        if (actionName != null) {
            if (action == null) {
                val finalSnackbar: Snackbar = snackbar
                action = InfoBarClickAction { finalSnackbar.dismiss() }
            }
            val finalAction: InfoBarClickAction = action
            snackbar.setAction(actionName, object : SafeClickListener() {
                override fun onClick() {
                    finalAction.onClick()
                }
            })
            if (color != null) {
                snackbar.setActionTextColor(color)
            }
        }
        snackbar.show()
        infobars[view] = snackbar
        logger.info(info)
    }

    open fun showInfo(info: String) {
        showActionInfo(info, gui.mainContent, "OK", null, null)
    }

    open fun showInfoCancellable(info: String, cancelCallback: InfoBarClickAction?) {
        showActionInfo(info, gui.mainContent, "Undo", cancelCallback, ContextCompat.getColor(activity, R.color.colorPrimary))
    }

    fun showToast(message: String) {
        GlobalScope.launch(Dispatchers.Main) {
            Toast.makeText(activity.applicationContext, message, Toast.LENGTH_LONG).show()
        }
        logger.debug("UI: toast: $message")
    }

}