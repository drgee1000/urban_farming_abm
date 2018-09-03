
package repastcity3.environment;

import java.util.ArrayList;
import java.util.List;


import com.vividsolutions.jts.geom.Coordinate;

public class Junction implements FixedGeography{
	
	public static int UniqueID = 0;
	private int id ;
	private Coordinate coord;
	private List<Road> roads; // The Roads connected to this Junction, used in GIS road network
	
	public Junction() {
		this.id = UniqueID++;
		this.roads = new ArrayList<Road>();
	}
	
	
	/**
	 * Get the junction's unique ID (these are assigned in incremental order as
	 * junctions are created.
	 */
	public int getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Junction "+this.id+" ("+this.coord.x+","+this.coord.y+")";
	}
	
	public List<Road> getRoads() {
		return this.roads;
	}
	
	public void addRoad(Road road) {
		this.roads.add(road);
	}
	
	/**
	 * Tests if Junctions are equal by comparing the coorinates.
	 * @param j The junction to be compared with this one
	 * @return True if their coordinates are equal, false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Junction)) {
			return false;
		}
		Junction j = (Junction) obj;
		return this.getCoords().equals(j.getCoords());
	}

	/**
	 * Get the coordinate of this junction
	 */
	public Coordinate getCoords() {
		return coord;
	}
	
	@Override
	public void setCoords(Coordinate c) {
		this.coord = c;
		
	}
	
}
