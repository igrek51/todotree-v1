package igrek.todotree.info

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import igrek.todotree.MainApplication
import igrek.todotree.R
import igrek.todotree.info.errorcheck.SafeClickListener
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
class Toaster(
    context: LazyInject<Context> = appFactory.context,
) {
    private val context by LazyExtractor(context)

    private val logger = LoggerFactory.logger
    private val infobars = HashMap<View?, Snackbar>()

    fun toast(message: String) {
        logger.info("UI: toast: $message")
        GlobalScope.launch(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    fun snackbar(message: String) {
        val myApp = context.applicationContext as? MainApplication?
        val activity = myApp?.currentActivityListener?.currentActivity

        if (activity != null) {
            showSnackbar(activity, message)
        } else {
            toast(message)
        }
    }

    private fun showSnackbar(
        activity: Activity,
        info: String,
        actionName: String = "OK",
        indefinite: Boolean = false,
        action: (() -> Unit)? = null, // dissmiss by default
    ) {
        GlobalScope.launch(Dispatchers.Main) {
            val snackbarLength = if (indefinite) Snackbar.LENGTH_INDEFINITE else Snackbar.LENGTH_LONG

            // dont create new snackbars if one is already shown
            val view: View = activity.findViewById(R.id.main_content_container)
            var snackbar: Snackbar? = infobars[view]
            if (snackbar == null || !snackbar.isShown) { // a new one
                snackbar = Snackbar.make(view, info, snackbarLength)
            } else { // visible - reuse it one more time
                snackbar.duration = snackbarLength
                snackbar.setText(info)
            }

            val actionV: () -> Unit = action ?: snackbar::dismiss
            snackbar.setAction(actionName, SafeClickListener {
                actionV.invoke()
            })
            val color = ContextCompat.getColor(activity, R.color.colorAccent)
            snackbar.setActionTextColor(color)

            snackbar.show()
            infobars[view] = snackbar
        }
        logger.info("UI: snackbar: $info")
    }

    fun error(t: Throwable, errorContext: String = "Error") {
        val myApp = context.applicationContext as? MainApplication?
        val activity = myApp?.currentActivityListener?.currentActivity

        logger.error(t)
        val err: String = when {
            t.message != null -> "${errorContext}: ${t.message}"
            else -> "${errorContext}: ${t::class.simpleName}"
        }

        if (activity != null) {
            showSnackbar(activity, err, indefinite = true, actionName = "Details") {
                showErrorDetails(activity, t, errorContext)
            }
        } else {
            toast("Error: $err")
        }
    }

    fun output(message: String) {
        val myApp = context.applicationContext as? MainApplication?
        val activity = myApp?.currentActivityListener?.currentActivity

        if (activity != null) {
            showSnackbar(activity, message, indefinite = true, actionName = "Details") {
                showOutputDetails(activity, message)
            }
        } else {
            toast(message)
        }
    }

    private fun showErrorDetails(
        activity: Activity, t: Throwable, errorContext: String,
    ) {
        val errorMessage = errorContext + ": " + t.message.orEmpty()
        val message = "${errorMessage}\nType: ${t::class.simpleName}"
        dialogThreeChoices(activity, title = "Error occurred", message = message)
    }

    private fun showOutputDetails(
        activity: Activity, details: String,
    ) {
        dialogThreeChoices(activity, title = "Details", message = details)
    }

    private fun dialogThreeChoices(
        activity: Activity,
        title: String = "",
        message: String = "",
    ) {
        GlobalScope.launch(Dispatchers.Main) {
            val alertBuilder = AlertDialog.Builder(activity)

            alertBuilder.setMessage(message)
            alertBuilder.setTitle(title)

            alertBuilder.setPositiveButton("OK") { _, _ -> }

            alertBuilder.setCancelable(true)
            val alertDialog = alertBuilder.create()
            if (!activity.isFinishing) {
                alertDialog.show()
            }
        }
    }

}