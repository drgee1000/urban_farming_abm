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
public class Residential implements FixedGeography{

/** A list of agents who stay here*/
	
	private List<IAgent> agents;
	
	/** A identifier for this residential */
	
	private String identifier;
	

	/**
	 * The coordinates of the residential. This is also stored by the projection that contains this Building but it is
	 * useful to have it here too. As they will never change (buildings don't move) we don't need to worry about keeping
	 * them in sync with the projection.
	 */
	
	private Coordinate coords;
	
	public Residential() {
		this.agents = new ArrayList<IAgent>();
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
			throw new NoIdentifierException("This residential has no identifier. This can happen "
					+ "when roads are not initialised correctly (e.g. there is no attribute "
					+ "called 'identifier' present in the shapefile used to create this Road)");
		} else {
			return identifier;
		}
	}
	
	
	public void setIdentifier(String id) {
		this.identifier = id;
	}

	public void addAgent(IAgent a) {
		this.agents.add(a);
	}

	public List<IAgent> getAgents() {
		return this.agents;
	}

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
	
	
	/**
	 * Return this residential unique id number.
	 */
	@Override
	public int hashCode() {
		return this.identifier.hashCode();
	}	

}
