package igrek.todotree.exceptions;


public class DatabaseLockedException extends RuntimeException {
	
	public DatabaseLockedException(){
		super("Database locked.");
	}
}
