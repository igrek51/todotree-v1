package igrek.todotree.exceptions;

public class NoSuperItemException extends Exception {
	public NoSuperItemException() {
		super();
	}
	
	public NoSuperItemException(String detailMessage) {
		super(detailMessage);
	}
}
