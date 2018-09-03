
package repastcity3.exceptions;

public class ParameterNotFoundException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public ParameterNotFoundException(String param) {
		super("Could not find the Simphony parameter "+param+
				". Has it been specified in parameters.xml ?");
	}

}
