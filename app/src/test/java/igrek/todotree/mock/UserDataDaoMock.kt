package igrek.todotree.mock

import igrek.todotree.inject.SingletonInject
import igrek.todotree.persistence.user.LazyDaoLoader
import igrek.todotree.persistence.user.UserDataDao
import igrek.todotree.persistence.user.preferences.PreferencesDao

class UserDataDaoMock : UserDataDao(
    localDataService = SingletonInject { null!! },
) {

    override var preferencesDao: PreferencesDao by LazyDaoLoader { path -> PreferencesDao(path) }

    override fun load() {}

    override fun save() {}

}