package igrek.todotree.services.lock;


public class DatabaseLock {
	
	private boolean locked = true;
	
	public DatabaseLock() {
	}
	
	public boolean isLocked() {
		return locked;
	}
	
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
}
