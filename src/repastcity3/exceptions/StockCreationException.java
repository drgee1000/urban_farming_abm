package repastcity3.exceptions;

public class StockCreationException extends Exception {
	private static final long serialVersionUID = 1L;
	public StockCreationException(String message) {
		super(message);
	}
	
	public StockCreationException(Throwable cause) {
		super(cause);
	}
}
