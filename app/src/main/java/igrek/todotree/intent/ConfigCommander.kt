package igrek.todotree.intent

import igrek.todotree.info.UiInfoService
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.settings.SettingsState
import java.util.*

class ConfigCommander(
    settingsState: LazyInject<SettingsState> = appFactory.settingsState,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
) {
    private val settingsState by LazyExtractor(settingsState)
    private val uiInfoService by LazyExtractor(uiInfoService)

    fun setDbLock(sVal: String) {
        val value = sVal.lowercase(Locale.ROOT)
        val valueB = value == "true" || value == "on" || value == "1"
        val property = "lockdb"
        settingsState.lockDB = valueB
        uiInfoService.showSnackbar("Setting saved: $property = $valueB")
    }

    fun setUserAuthToken(value: String) {
        val property = "userauthtoken"
        settingsState.userAuthToken = value
        uiInfoService.showSnackbar("Setting saved: $property = $value")
    }

}