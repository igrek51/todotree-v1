package igrek.todotree.services.lock;


import igrek.todotree.exceptions.DatabaseLockedException;
import igrek.todotree.services.preferences.Preferences;
import igrek.todotree.services.preferences.PropertyDefinition;

public class DatabaseLock {
	
	private boolean locked = true;
	
	private Preferences preferences;
	
	public DatabaseLock(Preferences preferences) {
		this.preferences = preferences;
		locked = preferences.getValue(PropertyDefinition.lockDB, Boolean.class);
	}
	
	public boolean isLocked() {
		return locked;
	}
	
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	
	public void assertUnlocked() throws DatabaseLockedException {
		if (locked)
			throw new DatabaseLockedException();
	}
}
