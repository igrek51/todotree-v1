package igrek.todotree.exceptions;


public class DeserializationFailedException extends RuntimeException {
	public DeserializationFailedException(String message) {
		super(message);
	}
}
