package igrek.todotree.service.access

import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.exceptions.DatabaseLockedException
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.settings.SettingsState

class DatabaseLock(
    settingsState: LazyInject<SettingsState> = appFactory.settingsState,
    accessLogService: LazyInject<AccessLogService> = appFactory.accessLogService,
) {
    private val accessLogService by LazyExtractor(accessLogService)

    var isLocked = true
    private val logger = LoggerFactory.logger

    fun unlockIfLocked(item: AbstractTreeItem?): Boolean {
        if (isLocked) {
            isLocked = false
            accessLogService.logDBUnlocked(item)
            logger.debug("Database unlocked.")
            return true
        }
        return false
    }

    @Throws(DatabaseLockedException::class)
    fun assertUnlocked() {
        if (isLocked) throw DatabaseLockedException()
    }

    init {
        isLocked = settingsState.get().lockDB
    }
}