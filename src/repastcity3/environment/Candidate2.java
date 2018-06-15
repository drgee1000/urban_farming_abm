/**
 * 
 */
package repastcity3.environment;

import java.util.logging.Logger;

import repast.simphony.query.space.gis.GeographyWithin;
import repastcity3.exceptions.NoIdentifierException;
import repastcity3.main.ContextManager;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author CHAO LUO
 *
 */
public class Candidate2 implements FixedGeography{
	
private static Logger LOGGER = Logger.getLogger(Candidate2.class.getName());
	
	/** A identifier for this charging station */
	
	public String identifier;
	
	private Coordinate coords;
	
	private int indRes = 0;
	
	private int indShop = 0;
	
	private double dis = 1000;
	
	public Candidate2() {
		//this.agents = new ArrayList<IAgent>();
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
			throw new NoIdentifierException("This charging station has no identifier. This can happen "
					+ "when roads are not initialised correctly (e.g. there is no attribute "
					+ "called 'identifier' present in the shapefile used to create this Road)");
		} else {
			return identifier;
		}
	}
	
	public void setIdentifier(String id) {
		this.identifier = id;
	}
	
	@Override
	public String toString() {
		return "charging station (Level 1): " + this.identifier;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Candidate2))
			return false;
		Candidate2 b = (Candidate2) obj;
		return this.identifier.equals(b.identifier);
	}
	
	@Override
	public int hashCode() {
		return this.identifier.hashCode();

	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int isRes() {
		
		Geometry location = ContextManager.candidate2Projection.getGeometry(this);
		GeographyWithin gquery = new GeographyWithin(ContextManager.restaurantProjection, this.dis, location);
		Iterable<Restaurant> it = gquery.query();
		if (it != null && it.iterator().hasNext()) {
			return 1;
		} else {
			return 0;
		}
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int isShop() {
		
		Geometry location = ContextManager.candidate2Projection.getGeometry(this);
		GeographyWithin gquery = new GeographyWithin(ContextManager.shoppingcenterProjection, this.dis, location);
		Iterable<Shoppingcenter> it = gquery.query();
		if (it != null && it.iterator().hasNext()) {
			return 1;
		} else {
			return 0;
		}
		
	}
	
	

}
