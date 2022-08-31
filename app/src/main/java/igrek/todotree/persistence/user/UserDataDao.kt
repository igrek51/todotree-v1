package igrek.todotree.persistence.user

import android.annotation.SuppressLint
import igrek.todotree.info.errorcheck.UiErrorHandler
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.persistence.LocalDataService
import igrek.todotree.persistence.user.preferences.PreferencesDao
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@SuppressLint("CheckResult")
open class UserDataDao(
    localDataService: LazyInject<LocalDataService> = appFactory.localDataService,
) {
    internal val localDataService by LazyExtractor(localDataService)

    open var preferencesDao: PreferencesDao by LazyDaoLoader { path -> PreferencesDao(path) }

    private var saveRequestSubject: PublishSubject<Boolean> = PublishSubject.create()
    private val logger = LoggerFactory.logger

    init {
        saveRequestSubject
            .throttleLast(2, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ toSave ->
                if (toSave)
                    save()
            }, UiErrorHandler::handleError)
        load()
    }

    protected open fun load() {
        val path = localDataService.appFilesDir.absolutePath

        preferencesDao = PreferencesDao(path)

        logger.debug("user data loaded")
    }

    @Synchronized
    protected open fun save() {
        preferencesDao.save()
        logger.info("user data saved")
    }

    fun factoryReset() {
        preferencesDao.factoryReset()
    }

    fun requestSave(toSave: Boolean) {
        saveRequestSubject.onNext(toSave)
    }

    fun saveNow() {
        requestSave(false)
        save()
    }

}

class LazyDaoLoader<T : AbstractJsonDao<out Any>>(
    private val loader: (path: String) -> T,
) : ReadWriteProperty<UserDataDao, T> {

    private var loaded: T? = null

    override fun getValue(thisRef: UserDataDao, property: KProperty<*>): T {
        val loadedVal = loaded
        if (loadedVal != null)
            return loadedVal

        val path = thisRef.localDataService.appFilesDir.absolutePath
        val loadedNN = loader.invoke(path)
        loaded = loadedNN
        return loadedNN
    }

    override fun setValue(thisRef: UserDataDao, property: KProperty<*>, value: T) {
        loaded = value
    }
}
