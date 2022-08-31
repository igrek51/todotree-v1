package igrek.todotree.service.commander

import igrek.todotree.info.UiInfoService
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.intent.ItemEditorCommand
import igrek.todotree.settings.SettingsState
import java.util.*

/**
 * secret commands parser:
 * ###dupa
 * ###config lockdb true
 * ###config lockdb false
 * ###config userauthtoken authToken
 * ###remote_item
 */
class SecretCommander(
    settingsState: LazyInject<SettingsState> = appFactory.settingsState,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
) {
    private val settingsState by LazyExtractor(settingsState)
    private val uiInfoService by LazyExtractor(uiInfoService)

    private val logger = LoggerFactory.logger

    fun execute(content: String): Boolean {
        if (!content.startsWith(CMD_PREFIX)) return false
        val commandsAll = content.substring(CMD_PREFIX.length)
        return if (commandsAll.isEmpty()) false else executeCommand(Arrays.asList(*commandsAll.split(" ").toTypedArray()))
    }

    private fun executeCommand(parts: List<String>): Boolean {
        val main = parts[0]
        if (main.isEmpty()) return false
        val params = parts.subList(1, parts.size)
        when (main) {
            "dupa" -> commandDupa(params)
            "config" -> commandConfig(params)
            "remote_item" -> createRemoteItem()
            else -> {
                uiInfoService.showSnackbar("Unknown command: $main")
                return false
            }
        }
        return true
    }

    private fun createRemoteItem() {
        ItemEditorCommand().createRemoteItem()
    }

    private fun commandDupa(params: List<String>) {
        uiInfoService.showSnackbar("Congratulations! You have discovered an Easter Egg.")
    }

    private fun commandConfig(params: List<String>) {
        if (params.size < 2) {
            logger.warn("not enough params")
            return
        }
        val property = params[0].toLowerCase()
        var value = params[1]
        when (property) {
            "lockdb" -> {
                value = value.toLowerCase()
                val valueB = value == "true" || value == "on" || value == "1"
                settingsState.lockDB = valueB
                uiInfoService.showSnackbar("Property saved: $property = $valueB")
            }
            "userauthtoken" -> {
                settingsState.userAuthToken = value
                uiInfoService.showSnackbar("Property saved: $property = $value")
            }
            else -> {
                logger.warn("unknown property: $property")
            }
        }
    }

    companion object {
        private const val CMD_PREFIX = "###"
    }

}