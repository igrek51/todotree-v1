package igrek.todotree.mock

import igrek.todotree.inject.SingletonInject
import igrek.todotree.persistence.user.LazyDaoLoader
import igrek.todotree.persistence.user.preferences.PreferencesDao
import igrek.todotree.settings.SettingsService


class SettingsServiceMock : SettingsService(
    userDataDao = SingletonInject { UserDataDaoMock() }
) {

    override fun loadAll() {}

}