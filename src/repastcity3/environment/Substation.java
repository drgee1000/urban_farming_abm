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
public class Substation extends Building{
	

	
	public String toString() {
		return "Substation : " + this.identifier;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Workplace))
			return false;
		Workplace b = (Workplace) obj;
		return this.identifier.equals(b.identifier);
	}
	
	

}
