/**
 * 
 */
package repastcity3.environment;

import java.util.ArrayList;
import java.util.List;

import repastcity3.agent.IAgent;
import repastcity3.exceptions.NoIdentifierException;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author CHAO LUO
 *
 */
public class Residential extends Building{

	@Override
	public String toString() {
		return "residential: " + this.identifier;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Residential))
			return false;
		Residential b = (Residential) obj;
		return this.identifier.equals(b.identifier);
	}
}
