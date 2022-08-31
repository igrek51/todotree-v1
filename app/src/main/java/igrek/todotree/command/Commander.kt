package igrek.todotree.command

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.View
import android.view.WindowManager.LayoutParams
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import igrek.todotree.MainApplication
import igrek.todotree.info.Toaster
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.appFactory
import igrek.todotree.intent.ConfigCommander
import igrek.todotree.intent.ItemEditorCommand
import igrek.todotree.service.permissions.PermissionsManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@OptIn(DelicateCoroutinesApi::class)
class Commander(
    val context: Context,
) {
    private val logger = LoggerFactory.logger
    private val toaster = Toaster(context)

    private val cmdRules: List<CommandRule> by lazy {
        listOf(
            CommandRule({
                it.matches("^m[ou]+$".toRegex())
            }) { showCowSuperPowers() },
            SimpleKeyRule("dupa") {
                showCowSuperPowers()
            },

            SimpleKeyRule("setup files") {
                PermissionsManager(context).setupFiles()
            },

            SimpleKeyRule("factory reset") {
                appFactory.userDataDao.get().factoryReset()
            },

            NestedSubcommandRule("config", rules=listOf(
                SubCommandRule("lockdb") { subcommand ->
                    ConfigCommander().setDbLock(subcommand)
                },
                SubCommandRule("userauthtoken") { subcommand ->
                    ConfigCommander().setUserAuthToken(subcommand)
                },
            )),

            NestedSubcommandRule("test", rules=listOf(
                SubCommandRule("error") { message ->
                    throw RuntimeException("fail: $message")
                },
                SubCommandRule("remote_item") { sub ->
                    ItemEditorCommand().createRemoteItem()
                },
            )),
        )
    }

    private fun commandAttempt(command: String) {
        logger.debug("command entered: $command")

        GlobalScope.launch(Dispatchers.Main) {
            checkActivationRules(command.trim())
        }
    }

    private fun checkActivationRules(command: String) {
        val activation = findActivator(cmdRules, command)

        if (activation == null) {
            toaster.snackbar("Invalid command: $command")
            return
        }

        toaster.snackbar("Command activated: $command")
        try {
            activation.run()
        } catch (t: Throwable) {
            toaster.error(t)
        }
    }

    fun showCommandAlert() {
        val myApp = context.applicationContext as MainApplication
        val activityContext = myApp.currentActivityListener.currentActivity ?: context

        val dlgAlert = AlertDialog.Builder(activityContext)
        dlgAlert.setMessage("Enter command:")
        dlgAlert.setTitle("Command")

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
            val myApp = context.applicationContext as MainApplication
            val activityContext = myApp.currentActivityListener.currentActivity ?: context

            val alertBuilder = AlertDialog.Builder(activityContext)
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
