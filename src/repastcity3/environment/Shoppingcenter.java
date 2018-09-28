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
public class Shoppingcenter extends Building{

	@Override
	public String toString() {
		return "shoppingcenter: " + this.identifier;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Shoppingcenter))
			return false;
		Shoppingcenter b = (Shoppingcenter) obj;
		return this.identifier.equals(b.identifier);
	}

}
