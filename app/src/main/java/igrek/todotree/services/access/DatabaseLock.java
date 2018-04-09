package igrek.todotree.services.access;


import igrek.todotree.exceptions.DatabaseLockedException;
import igrek.todotree.logger.Logs;
import igrek.todotree.services.preferences.Preferences;
import igrek.todotree.services.preferences.PropertyDefinition;

public class DatabaseLock {
	
	private boolean locked = true;
	
	private Logs logger;
	private AccessLogService accessLogService;
	
	public DatabaseLock(Preferences preferences, Logs logger, AccessLogService accessLogService) {
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
	
	public boolean unlockIfLocked() {
		if (isLocked()) {
			setLocked(false);
			accessLogService.logDBUnlocked();
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
