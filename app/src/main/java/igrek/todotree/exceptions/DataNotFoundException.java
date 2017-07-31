package igrek.todotree.exceptions;

public class DataNotFoundException extends Exception {
	public DataNotFoundException() {
		super();
	}
	
	public DataNotFoundException(String detailMessage) {
		super(detailMessage);
	}
}
