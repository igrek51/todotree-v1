package igrek.todotree.service.commander

import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.service.preferences.Preferences
import igrek.todotree.service.preferences.PropertyDefinition
import igrek.todotree.service.resources.UserInfoService
import java.util.*

/**
 * secret commands parser:
 * ###dupa
 * ###config lockdb true
 * ###config lockdb false
 * ###config dbFilePath /storage/6D49-845B/Android/data/igrek.todotree/todo.json
 * ###config userAuthToken authToken
 */
class SecretCommander(private val preferences: Preferences, private val userInfo: UserInfoService) {

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
            else -> {
                userInfo.showInfo("Unknown command: $main")
                return false
            }
        }
        return true
    }

    private fun commandDupa(params: List<String>) {
        userInfo.showInfo("Congratulations! You have discovered an Easter Egg.")
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
                preferences.setValue(PropertyDefinition.lockDB, valueB)
                userInfo.showInfo("Property saved: $property = $valueB")
            }
            "userAuthToken" -> {
                preferences.setValue(PropertyDefinition.userAuthToken, value)
                userInfo.showInfo("Property saved: $property = $value")
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