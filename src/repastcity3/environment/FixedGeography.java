
package repastcity3.environment;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Implemented by all objects which hold their own Coord objects in conjunction with those
 * held by any projections they exist in. For example, a Road object has road coordinates stored
 * by the ROAD_ENVIRONMENT, but these coordinates can also be found (for simplicity) by calling
 * road.getCoords().<br>
 * Used by EnvironmentFactory.readShapeFile().<br>
 * Must not be used by objects which will move such as People.
 * @author Nick Malleson
 *
 */
public interface FixedGeography {
	
	Coordinate getCoords();
	void setCoords(Coordinate c);

}
