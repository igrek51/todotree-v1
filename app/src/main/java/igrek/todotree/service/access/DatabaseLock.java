package igrek.todotree.service.access;


import igrek.todotree.domain.treeitem.AbstractTreeItem;
import igrek.todotree.exceptions.DatabaseLockedException;
import igrek.todotree.logger.Logger;
import igrek.todotree.service.preferences.Preferences;
import igrek.todotree.service.preferences.PropertyDefinition;

public class DatabaseLock {
	
	private boolean locked = true;
	
	private Logger logger;
	private AccessLogService accessLogService;
	
	public DatabaseLock(Preferences preferences, Logger logger, AccessLogService accessLogService) {
		locked = preferences.getValue(PropertyDefinition.lockDB, Boolean.class);
		this.logger = logger;
		this.accessLogService = accessLogService;
	}
	
	public boolean isLocked() {
		return locked;
	}
	
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	
	public boolean unlockIfLocked(AbstractTreeItem item) {
		if (isLocked()) {
			setLocked(false);
			accessLogService.logDBUnlocked(item);
			logger.debug("Database unlocked.");
			return true;
		}
		return false;
	}
	
	public void assertUnlocked() throws DatabaseLockedException {
		if (locked)
			throw new DatabaseLockedException();
	}
}
