package igrek.todotree.service.access

import igrek.todotree.info.logger.LoggerFactory.logger
import igrek.todotree.service.preferences.Preferences.getValue
import igrek.todotree.service.access.AccessLogService.logDBUnlocked
import igrek.todotree.info.logger.Logger.debug
import igrek.todotree.service.access.AccessLogService
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.domain.treeitem.AbstractTreeItem
import kotlin.Throws
import igrek.todotree.exceptions.DatabaseLockedException
import igrek.todotree.service.preferences.Preferences
import igrek.todotree.service.preferences.PropertyDefinition

class DatabaseLock(preferences: Preferences, accessLogService: AccessLogService) {
    var isLocked = true
    private val logger = LoggerFactory.logger
    private val accessLogService: AccessLogService
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
        isLocked = preferences.getValue(PropertyDefinition.lockDB, Boolean::class.java)!!
        this.accessLogService = accessLogService
    }
}