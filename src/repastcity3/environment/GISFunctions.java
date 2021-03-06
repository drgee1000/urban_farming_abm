
package repastcity3.environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import repast.simphony.context.Context;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.ShapefileLoader;
import repast.simphony.space.graph.Network;
import repastcity3.agent.IAgent;
import repastcity3.exceptions.NoIdentifierException;
import repastcity3.utilities.Helper;

/**
 * Class with useful GIS functions for configuring the GIS model environment.
 * 
 * @author Nick Malleson
 * 
 */
public class GISFunctions {

	private static Logger LOGGER = Logger.getLogger(Route.class.getName());

	/**
	 * Create the road network. Runs through the roads in the <code>roadGeography</code> and, for each one, will create
	 * <code>Junction</code> objects at their end points and an edge linking them. The <code>Junction</code> objects are
	 * added to the given <code>junctionGeography</code> (so that we know where they are spatially) and they are also
	 * added, along with the edge between them, to the <code>junctionNetwork</code> so that topographical relationships
	 * can be established. (The <code>junctionNetwork</code> is part of the <code>junctionContext</code>
	 * 
	 * @param roadGeography
	 * @param junctionContext
	 * @param junctionGeography
	 * @param roadNetwork
	 * @throws NoIdentifierException 
	 */
	public static void buildGISRoadNetwork(Geography<Road> roadGeography, Context<Junction> junctionContext,
			Geography<Junction> junctionGeography, Network<Junction> roadNetwork) throws NoIdentifierException {

		// Create a GeometryFactory so we can create points/lines from the junctions and roads
		// (this is so they can be displayed on the same display to check if the network has been created successfully)
		GeometryFactory geomFac = new GeometryFactory();

		// Create a cache of all Junctions and coordinates so we know if a junction has already been created at a
		// particular coordinate
		Map<Coordinate, Junction> coordMap = new HashMap<Coordinate, Junction>();

		// Iterate through all roads
		Iterable<Road> roadIt = roadGeography.getAllObjects();
		int last = 1;
		for (Road road : roadIt) {
			// Create a LineString from the road so we can extract coordinates
			Geometry roadGeom = roadGeography.getGeometry(road);
			Coordinate c1 = roadGeom.getCoordinates()[0]; // First coord
			Coordinate c2 = roadGeom.getCoordinates()[roadGeom.getNumPoints() - 1]; // Last coord
			
			// Create Junctions from these coordinates and add them to the JunctionGeography (if they haven't been
			// created already)
			Junction junc1, junc2;
			if (coordMap.containsKey(c1)) {
				// A Junction with those coordinates (c1) has been created, get it so we can add an edge to it
				junc1 = coordMap.get(c1);
			} else { // Junction does not exit
				junc1 = new Junction();
				junc1.setCoords(c1);
				junctionContext.add(junc1); // Add new junction to the corresponding context
				coordMap.put(c1, junc1);
				Point p1 = geomFac.createPoint(c1); 
				junctionGeography.move(junc1, p1);// Add new junction to the corresponding projection
			}
			if (coordMap.containsKey(c2)) {
				junc2 = coordMap.get(c2);
			} else { // Junction does not exit
				junc2 = new Junction();
				junc2.setCoords(c2);
				junctionContext.add(junc2);
				coordMap.put(c2, junc2);
				Point p2 = geomFac.createPoint(c2);
				junctionGeography.move(junc2, p2);
			}
			// Tell the road object who it's junctions are
			road.addJunction(junc1);
			road.addJunction(junc2);
			// Tell the junctions about this road
			junc1.addRoad(road);
			junc2.addRoad(road);

			// Create an edge between the two junctions, assigning a weight equal to it's length
			NetworkEdge<Junction> edge = new NetworkEdge<Junction>(junc1, junc2, false, roadGeom.getLength(), road
					.getAccessibility());
			//System.out.print(i + "\n");
			//i++;
			// Set whether or not the edge represents a major road (gives extra benefit to car drivers).
			
			//String luo = road.getIdentifier();
			//System.out.print(road.getIdentifier() + ",");
			
			
			//System.out.print(i + ",");
			if (road.isMajorRoad()){
				edge.setMajorRoad(true);
			}
			// // Store the road's TOID in a dictionary (one with edges as keys, one with id's as keys)
			// try {
			// // edgeIDs_KeyEdge.put(edge, (String) road.getIdentifier());
			// // edgeIDs_KeyID.put((String) road.getIdentifier(), edge);
			// edges_roads.put(edge, road);
			// roads_edges.put(road, edge);
			// } catch (Exception e) {
			// Outputter.errorln("EnvironmentFactory: buildGISRoadNetwork error, here's the message:\n"+e.getMessage());
			// }
			// Tell the Road and the Edge about each other
			road.setEdge(edge);
			edge.setRoad(road);
			//System.out.print("Edge number is " + roadNetwork.size() + "\n");
			if (!roadNetwork.containsEdge(edge)) {
				roadNetwork.addEdge(edge);
				if (last != Helper.sizeOfIterable(roadNetwork.getEdges())){
					System.out.print(road.getIdentifier()+"\n");
					last--;
				}
					last++;
//				System.out.print(i + "\n");
//				i++;
//				System.out.print("Edge number is " + sizeOfIterable(roadNetwork.getEdges()) + "\n");
//				System.out.print(sizeOfIterable(roadNetwork.getEdges()));
			} else {
				LOGGER.severe("CityContext: buildRoadNetwork: for some reason this edge that has just been created "
						+ "already exists in the RoadNetwork!");
			}

		} // for road:
	}
	
	//System.out.print(sizeOfIterable(roadNetwork.getEdges()));

	/**
	 * Nice generic function :-) that reads in objects from shapefiles.
	 * <p>
	 * The objects (agents) created must extend FixedGeography to guarantee that they will have a setCoords() method.
	 * This is necessary because, for simplicity, geographical objects which don't move store their coordinates
	 * alongside the projection which stores them as well. So the coordinates must be set manually by this function once
	 * the shapefile has been read and the objects have been given coordinates in their projection.
	 * 
	 * @param <T>
	 *            The type of object to be read (e.g. PecsHouse). Must exted
	 * @param cl
	 *            The class of the building being read (e.g. PecsHouse.class).
	 * @param shapefileLocation
	 *            The location of the shapefile containing the objects.
	 * @param geog
	 *            A geography to add the objects to.
	 * @param context
	 *            A context to add the objects to.
	 * @throws MalformedURLException
	 *             If the location of the shapefile cannot be converted into a URL
	 * @throws FileNotFoundException
	 *             if the shapefile does not exist.
	 * @see FixedGeography
	 */
	public static <T extends FixedGeography> void readShapefile(Class<T> cl, String shapefileLocation,
			Geography<? super T> geog, Context<? super T> context) throws MalformedURLException, FileNotFoundException {
		File shapefile = null;
		ShapefileLoader<T> loader = null;
		shapefile = new File(shapefileLocation);
		if (!shapefile.exists()) {
			throw new FileNotFoundException("Could not find the given shapefile: " + shapefile.getAbsolutePath());
		}
		loader = new ShapefileLoader<T>(cl, shapefile.toURI().toURL(), geog, context);
		loader.load();
		
//		// for FixedGeography interface
//		for (Object _obj : context.getObjects(cl)) {
//			Building obj=(Building)_obj;
//			obj.setCoords(geog.getGeometry(obj).getCentroid().getCoordinate());
//		}
	}

	/**
	 * An alternative to <code>readShapefile()</code> that does not require objects to implement
	 * <code>FixedGeography<code>. Hence it can be used by objects that don't store their coordinates internally (such 
	 * as agents).
	 * 
	 * @param <T>
	 *            The type of object to be read (e.g. PecsHouse). Must exted
	 * @param cl
	 *            The class of the building being read (e.g. PecsHouse.class).
	 * @param shapefileLocation
	 *            The location of the shapefile containing the objects.
	 * @param geog
	 *            A geography to add the objects to.
	 * @param context
	 *            A context to add the objects to.
	 * @throws MalformedURLException
	 *             If the location of the shapefile cannot be converted into a URL
	 * @throws FileNotFoundException
	 *             if the shapefile does not exist.
	 * @see FixedGeography
	 */
	public static <T extends IAgent> void readAgentShapefile(Class<T> cl, String shapefileLocation, Geography<? super T> geog,
			Context<? super T> context) throws MalformedURLException, FileNotFoundException {
		
		File shapefile = null;
		ShapefileLoader<T> loader = null;
		shapefile = new File(shapefileLocation);
		if (!shapefile.exists()) {
			throw new FileNotFoundException("Could not find the given shapefile: " + shapefile.getAbsolutePath());
		}
		loader = new ShapefileLoader<T>(cl, shapefile.toURI().toURL(), geog, context);
		loader.load();
	}
	
	private static List<SimpleFeature> loadFeaturesFromShapefile(String filename){
		URL url = null;
		try {
			url = new File(filename).toURL();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		List<SimpleFeature> features = new ArrayList<SimpleFeature>();
		
		// Try to load the shapefile
		SimpleFeatureIterator fiter = null;
		ShapefileDataStore store = null;
		store = new ShapefileDataStore(url);

		try {
			fiter = store.getFeatureSource().getFeatures().features();

			while(fiter.hasNext()){
				features.add(fiter.next());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			fiter.close();
			store.dispose();
		}
		
		return features;
	}
	
	public static Geometry readBoundaryShapefile(String boundaryFilename)
	{
		List<SimpleFeature> features = loadFeaturesFromShapefile(boundaryFilename);
		Geometry boundary = (MultiPolygon)features.iterator().next().getDefaultGeometry();
		return boundary;
	}
	
	public static List<Geometry> readAreaBoundaryShapefile(String boundaryFilename)
	{
		List<Geometry> boundarys=new ArrayList<>();
		List<SimpleFeature> features = loadFeaturesFromShapefile(boundaryFilename);	
		for(SimpleFeature sf:features)
		{
			Geometry boundary = (MultiPolygon)sf.getDefaultGeometry();
			boundarys.add(boundary);
		}
		return boundarys;
	}


}
