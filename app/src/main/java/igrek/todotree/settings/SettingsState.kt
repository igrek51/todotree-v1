package igrek.todotree.settings

import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class PreferencesState(
    settingsService: LazyInject<SettingsService> = appFactory.settingsService,
) {
    internal val preferencesService by LazyExtractor(settingsService)

    var lockDB: Boolean by PreferenceDelegate(SettingsField.LockDB)
    var userAuthToken: String by PreferenceDelegate(SettingsField.UserAuthToken)

}

class PreferenceDelegate<T : Any>(
        private val field: SettingsField
) : ReadWriteProperty<PreferencesState, T> {

    override fun getValue(thisRef: PreferencesState, property: KProperty<*>): T {
        return thisRef.preferencesService.getValue(field)
    }

    override fun setValue(thisRef: PreferencesState, property: KProperty<*>, value: T) {
        thisRef.preferencesService.setValue(field, value)
    }
}
