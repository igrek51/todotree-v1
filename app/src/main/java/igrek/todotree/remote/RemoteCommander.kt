package igrek.todotree.remote

import android.content.Context
import android.widget.EditText
import android.text.InputType
import androidx.appcompat.app.AlertDialog
import igrek.todotree.info.logger.LoggerFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.view.WindowManager.LayoutParams
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager

class RemoteCommander(
    val context: Context,
) {

    private val logger = LoggerFactory.logger

    private val cmdRules: List<CommandRule> by lazy {
        listOf(
            CommandRule({
                it.matches("^m[ou]+$".toRegex())
            }) { showCowSuperPowers() },
            SimplifiedKeyRule("dupa") {
                showCowSuperPowers()
            },

            SimplifiedKeyRule("unmute") {
                VolumeManager(context).unmute()
            },
            SimplifiedKeyRule("vibrate") {
                VolumeManager(context).setVibrateMode()
            },
            SimplifiedKeyRule("silent") {
                VolumeManager(context).setSilentMode()
            },

            SimplifiedKeyRule("maxvol", "volmax", "volumemax", "maxvolume", "max vol", "vol max") {
                VolumeManager(context).setMaxRingVolume()
            },
        )
    }

    fun commandAttempt(command: String) {
        logger.info("secret command entered: $command")

        GlobalScope.launch(Dispatchers.Main) {
            if (!checkActivationRules(command)) {
                logger.debug("Invalid command: $command")
            }
        }
    }

    private fun checkActivationRules(key: String): Boolean {
        for (rule in cmdRules) {
            if (rule.condition(key)) {
                logger.debug("rule activated: $key")
                rule.activator(key)
                return true
            }
        }
        return false
    }

    fun showCommandAlert() {
        val secretTitle = "Command"
        val dlgAlert = AlertDialog.Builder(context)
        dlgAlert.setMessage("Enter command:")
        dlgAlert.setTitle(secretTitle)

        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        input.isSingleLine = false
        dlgAlert.setView(input)

        dlgAlert.setPositiveButton("OK") { _, _ -> commandAttempt(input.text.toString()) }
        dlgAlert.setNegativeButton("Cancel") { _, _ -> }
        dlgAlert.setCancelable(true)
        val dialog = dlgAlert.create()
        dialog.window?.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.show()

        input.requestFocus()
        Handler(Looper.getMainLooper()).post {
            showSoftKeyboard(input)
        }
    }

    private fun showSoftKeyboard(view: View?) {
        val imm: InputMethodManager? = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(view, 0)
    }

    private fun showCowSuperPowers() {
        GlobalScope.launch(Dispatchers.Main) {
            val alertBuilder = AlertDialog.Builder(context)
            alertBuilder.setTitle("Moooo!")
            alertBuilder.setPositiveButton("OK") { _, _ -> }
            alertBuilder.setCancelable(true)
            val dialog: AlertDialog = alertBuilder.create()
            dialog.setMessage(EA5T3R_M00.trimIndent())
            dialog.show()
        }
    }

    companion object {
        private const val EA5T3R_M00: String = """
     _____________________
    / Congratulations!    \
    |                     |
    | You have found a    |
    \ Secret Cow Level :) /
     ---------------------
       \   ^__^
        \  (oo)\_______
           (__)\       )\/\
               ||----w |
               ||     ||
    """
    }
}