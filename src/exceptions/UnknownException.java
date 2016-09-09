package exceptions;

public class UnknownException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UnknownException(String message) {
		super(message);
	}
	
}
