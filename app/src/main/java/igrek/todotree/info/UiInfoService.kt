package igrek.todotree.info

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import igrek.todotree.R
import igrek.todotree.info.errorcheck.SafeClickListener
import igrek.todotree.info.errorcheck.SafeExecutor
import igrek.todotree.info.logger.Logger
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


@OptIn(DelicateCoroutinesApi::class)
open class UiInfoService(
    private val activity: LazyInject<Activity?> = appFactory.activity,
    context: LazyInject<Context> = appFactory.context,
) {
    private val context by LazyExtractor(context)

    private val infobars = HashMap<View?, Snackbar>()
    private val logger: Logger = LoggerFactory.logger

    open fun showSnackbar(
            info: String = "",
            infoResId: Int = 0,
            actionResId: Int = 0,
            action: (() -> Unit)? = null, // dissmiss by default
            indefinite: Boolean = false,
    ) {
        GlobalScope.launch(Dispatchers.Main) {
            val snackbarLength = if (indefinite) Snackbar.LENGTH_INDEFINITE else Snackbar.LENGTH_LONG
            val infoV = info.takeIf { it.isNotEmpty() } ?: resString(infoResId)

            val activity = activity.get() ?: return@launch

            // dont create new snackbars if one is already shown
            val view: View = activity.findViewById(R.id.main_content)
            var snackbar: Snackbar? = infobars[view]
            if (snackbar == null || !snackbar.isShown) { // a new one
                snackbar = Snackbar.make(view, infoV, snackbarLength)
            } else { // visible - reuse it one more time
                snackbar.duration = snackbarLength
                snackbar.setText(infoV)
            }

            if (actionResId > 0) {
                val actionV: () -> Unit = action ?: snackbar::dismiss
                val actionName = resString(actionResId)
                snackbar.setAction(actionName, SafeClickListener {
                    actionV.invoke()
                })
                val color = ContextCompat.getColor(context, R.color.colorAccent)
                snackbar.setActionTextColor(color)
            }

            snackbar.show()
            infobars[view] = snackbar
        }
        logger.info("UI: snackbar: $info")
    }

    fun showInfo(infoResId: Int, vararg args: String?,
                 indefinite: Boolean = false) {
        val info = resString(infoResId, *args)
        showSnackbar(info = info, actionResId = R.string.action_info_ok, indefinite = indefinite)
    }

    fun showInfoAction(infoResId: Int, vararg args: String,
                       indefinite: Boolean = false, actionResId: Int, action: () -> Unit) {
        val info = resString(infoResId, *args)
        showSnackbar(info = info, actionResId = actionResId, action = action, indefinite = indefinite)
    }


    open fun showToast(message: String) {
        GlobalScope.launch(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
        logger.debug("UI: toast: $message")
    }

    fun showToast(messageRes: Int) {
        val message = resString(messageRes)
        showToast(message)
    }

    fun showTooltip(infoRes: Int) {
        dialog(R.string.tooltip, infoRes)
    }

    fun clearSnackBars() {
        infobars.forEach { (_, snackbar) ->
            if (snackbar.isShown)
                snackbar.dismiss()
        }
        infobars.clear()
    }

    fun resString(resourceId: Int): String {
        return try {
            context.resources.getString(resourceId)
        } catch (e: Resources.NotFoundException) {
            ""
        }
    }

    fun resString(resourceId: Int, vararg args: Any?): String {
        val message = resString(resourceId)
        return if (args.isNotEmpty()) {
            String.format(message, *args)
        } else {
            message
        }
    }

    fun dialog(titleResId: Int, message: String) {
        dialogThreeChoices(
                titleResId = titleResId,
                message = message,
                positiveButton = R.string.action_info_ok, positiveAction = {},
        )
    }

    fun dialog(titleResId: Int, messageResId: Int) {
        dialogThreeChoices(
                titleResId = titleResId,
                messageResId = messageResId,
                positiveButton = R.string.action_info_ok, positiveAction = {},
        )
    }

    fun dialogThreeChoices(
            titleResId: Int = 0, title: String = "",
            messageResId: Int = 0, message: String = "",
            positiveButton: Int = 0, positiveAction: () -> Unit = {},
            negativeButton: Int = 0, negativeAction: () -> Unit = {},
            neutralButton: Int = 0, neutralAction: () -> Unit = {},
            postProcessor: (AlertDialog) -> Unit = {},
    ) {
        GlobalScope.launch(Dispatchers.Main) {
            val alertBuilder = AlertDialog.Builder(context)

            alertBuilder.setMessage(when {
                messageResId > 0 -> resString(messageResId)
                else -> message
            })
            alertBuilder.setTitle(when {
                titleResId > 0 -> resString(titleResId)
                else -> title
            })

            if (positiveButton > 0) {
                alertBuilder.setPositiveButton(resString(positiveButton)) { _, _ ->
                    SafeExecutor {
                        positiveAction.invoke()
                    }
                }
            }
            if (negativeButton > 0) {
                alertBuilder.setNegativeButton(resString(negativeButton)) { _, _ ->
                    SafeExecutor {
                        negativeAction.invoke()
                    }
                }
            }
            if (neutralButton > 0) {
                alertBuilder.setNeutralButton(resString(neutralButton)) { _, _ ->
                    SafeExecutor {
                        neutralAction.invoke()
                    }
                }
            }
            alertBuilder.setCancelable(true)
            val alertDialog = alertBuilder.create()
            postProcessor.invoke(alertDialog)
            activity.get()?.let { activity ->
                if (!activity.isFinishing) {
                    alertDialog.show()
                }
            }
        }
    }

}
