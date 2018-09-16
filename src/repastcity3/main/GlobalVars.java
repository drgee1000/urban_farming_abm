
package repastcity3.main;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.monitor.FileAlterationListener;

import com.vividsolutions.jts.geom.Geometry;

import repastcity3.agent.IAgent;
import repastcity3.agent.People;
import repastcity3.environment.Route;

public abstract class GlobalVars {
	
	private static Logger LOGGER = Logger.getLogger(GlobalVars.class.getName());
	
	/* These are strings that match entries in the repastcity.properties file.*/
	public static final String GISDataDirectory = "GISDataDirectory";
	public static final String BuildingShapefile = "BuildingShapefile";
	public static final String RoadShapefile = "RoadShapefile";
	public static final String ResidentialShapefile = "ResidentialShapefile";
	public static final String ShoppingcenterShapefile = "ShoppingcenterShapefile";
	public static final String FarmShapefile = "FarmShapefile";
	public static final String WorkplaceShapefile = "WorkplaceShapefile";
	public static final String RestaurantShapefile = "RestaurantShapefile";
	public static final String CandidateShapefile = "CandidateShapefile";
	public static final String BuildingsRoadsCoordsCache = "BuildingsRoadsCoordsCache";
	public static final String BuildingsRoadsCache = "BuildingsRoadsCache";
	public static final String SubstationShapefile = "SubstationShapefile";
	public static final String SchoolShapefile="SchoolShapefile";
	
	
	
	public static final class GEOGRAPHY_PARAMS {
		
		/**
		 * Different search distances used in functions that need to find objects that are
		 * close to them. A bigger buffer means that more objects will be analysed (less
		 * efficient) but if the buffer is too small then no objects might be found. 
		 * The units represent a lat/long distance so I'm not entirely sure what they are,
		 * but the <code>Route.distanceToMeters()</code> method can be used to roughly 
		 * convert between these units and meters.
		 * @see Geometry
		 * @see Route
		 */
		public enum BUFFER_DISTANCE {
			/** The smallest distance, rarely used. Approximately 0.001m*/
			SMALL(0.00000001, "0.001"),
			/** Most commonly used distance, OK for looking for nearby houses or roads.
			 * Approximatey 110m */
			MEDIUM(0.001,"110"),
			/** Largest buffer, approximately 550m. I use this when doing things that
			 * don't need to be done often, like populating caches.*/
			LARGE(0.08,"8800");
			/**
			 * @param dist The distance to be passed to the search function (in lat/long?)
			 * @param distInMeters An approximate equivalent distance in meters.
			 */
			BUFFER_DISTANCE(double dist, String distInMeters) {
				this.dist = dist;
				this.distInMeters = distInMeters;
			}
			public double dist;
			public String distInMeters;
		}

		public static final double TRAVEL_PER_TURN = 500; // TODO Make a proper value for this
		
		public static final double QUERY_DISTANCE = 2000; // Query distance for each charging station candidate
	}
	
	/** Names of contexts and projections. These names must match those in the
	 * parameters.xml file so that they can be displayed properly in the GUI. */
	public static final class CONTEXT_NAMES {
		
		public static final String MAIN_CONTEXT = "maincontext";
		public static final String MAIN_GEOGRAPHY = "MainGeography";
		
		public static final String BUILDING_CONTEXT = "BuildingContext";
		public static final String BUILDING_GEOGRAPHY = "BuildingGeography";
		
		public static final String ROAD_CONTEXT = "RoadContext";
		public static final String ROAD_GEOGRAPHY = "RoadGeography";
		
		public static final String JUNCTION_CONTEXT = "JunctionContext";
		public static final String JUNCTION_GEOGRAPHY = "JunctionGeography";
		
		public static final String ROAD_NETWORK = "RoadNetwork";
		
		public static final String AGENT_CONTEXT = "AgentContext";
		public static final String AGENT_GEOGRAPHY = "AgentGeography";
		
		public static final String RESIDENTIAL_CONTEXT = "ResidentialContext";
		public static final String RESIDENTIAL_GEOGRAPHY = "ResidentialGeography";
		
		public static final String WORKPLACE_CONTEXT = "WorkplaceContext";
		public static final String WORKPLACE_GEOGRAPHY = "WorkplaceGeography";
		
		public static final String SHOPPINGCENTER_CONTEXT = "ShoppingContext";
		public static final String SHOPPINGCENTER_GEOGRAPHY = "ShoppingGeography";
		
		public static final String RESTAURANT_CONTEXT = "RestaurantContext";
		public static final String RESTAURANT_GEOGRAPHY = "RestaurantGeography";
		
		public static final String Farm_CONTEXT = "FarmContext";
		public static final String Farm_GEOGRAPHY = "FarmGeography";
		
		public static final String SUBSTATION_CONTEXT = "SubstationContext";
		public static final String SUBSTATION_GEOGRAPHY = "SubstationGeography";
		
		public static final String SCHOOL_CONTEXT = "SchoolContext";
		public static final String SCHOOL_GEOGRAPHY = "SchoolGeography";
		
		
	
	}
	
	// Parameters used by transport networks
	public static final class TRANSPORT_PARAMS {

		// This variable is used by NetworkEdge.getWeight() function so that it knows what travel options
		// are available to the agent (e.g. has a car). Can't be passed as a parameter because NetworkEdge.getWeight()
		// must override function in RepastEdge because this is the one called by ShortestPath.
		public static People currentAgent = null;
		public static Object currentBurglarLock = new Object();

		public static final String WALK = "walk";
		public static final String BUS = "bus";
		public static final String TRAIN = "train";
		public static final String CAR = "car";
		// List of all transport methods in order of quickest first
		public static final List<String> ALL_PARAMS = Arrays.asList(new String[]{TRAIN, CAR, BUS, WALK});

		// Used in 'access' field by Roads to indicate that they are a 'majorRoad' (i.e. motorway or a-road).
		public static final String MAJOR_ROAD = "majorRoad";		
		// Speed advantage for car drivers if the road is a major road'
		public static final double MAJOR_ROAD_ADVANTAGE = 3;

		// The speed associated with different types of road (a multiplier, i.e. x times faster than walking)
		public static double getSpeed(String type) {
			if (type.equals(WALK))
				return 1;
			else if (type.equals(BUS))
				return 2;
			else if (type.equals(TRAIN))
				return 10;
			else if (type.equals(CAR))
				return 5;
			else {
				LOGGER.log(Level.SEVERE, "Error getting speed: unrecognised type: "+type);
				return 1;
			}
		}
	}
	

}
