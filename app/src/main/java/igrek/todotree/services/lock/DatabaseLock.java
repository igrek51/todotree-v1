package igrek.todotree.services.lock;


import igrek.todotree.exceptions.DatabaseLockedException;

public class DatabaseLock {
	
	private boolean locked = true;
	
	public DatabaseLock() {
	}
	
	public boolean isLocked() {
		throw new DatabaseLockedException();
	}
	
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	
	public void assertUnlocked() throws DatabaseLockedException {
		if (locked)
			throw new DatabaseLockedException();
	}
}
