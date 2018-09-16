/**
 * 
 */
package repastcity3.environment;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

import repastcity3.agent.IAgent;
import repastcity3.exceptions.NoIdentifierException;

/**
 * @author CHAO LUO
 *
 */
public class Workplace extends Building{
	
	@Override
	public String toString() {
		return "workplace: " + this.identifier;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Workplace))
			return false;
		Workplace b = (Workplace) obj;
		return this.identifier.equals(b.identifier);
	}

}
