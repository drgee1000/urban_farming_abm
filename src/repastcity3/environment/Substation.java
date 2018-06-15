/**
 * 
 */
package repastcity3.environment;

import java.util.logging.Logger;

import repastcity3.exceptions.NoIdentifierException;

import com.vividsolutions.jts.geom.Coordinate;


/**
 * @author CHAO LUO
 *
 */
public class Substation implements FixedGeography{
	
	private static Logger LOGGER = Logger.getLogger(Substation.class.getName());
	
	/** A identifier for this charging station */
	public String identifier;
	
	private Coordinate coords;
	
	// Constructor
	public Substation() {
		
	}
	
	@Override
	public Coordinate getCoords() {
		return this.coords;
	}

	@Override
	public void setCoords(Coordinate c) {
		this.coords = c;

	}
	
	public String getIdentifier() throws NoIdentifierException {
		if (this.identifier == null) {
			throw new NoIdentifierException("This substation has no identifier. This can happen "
					+ "when roads are not initialised correctly (e.g. there is no attribute "
					+ "called 'identifier' present in the shapefile used to create this Road)");
		} else {
			return identifier;
		}
	}
	
	
	public void setIdentifier(String id) {
		this.identifier = id;
	}
	
	
	public String toString() {
		return "Substation : " + this.identifier;
	}
	
	
	public boolean equals(Object obj) {
		if (!(obj instanceof Substation))
			return false;
		Substation b = (Substation) obj;
		return this.identifier.equals(b.identifier);
	}
	
	public int hashCode() {
		return this.identifier.hashCode();

	}
	
	

}
