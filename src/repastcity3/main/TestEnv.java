package repastcity3.main;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;

import repast.simphony.context.Context;
import repast.simphony.space.graph.Network;
import repastcity3.environment.Junction;
import repastcity3.environment.Residential;
import repastcity3.environment.Restaurant;
import repastcity3.environment.Road;
import repastcity3.environment.Shoppingcenter;
import repastcity3.environment.Workplace;
import repastcity3.exceptions.EnvironmentError;
import repastcity3.exceptions.NoIdentifierException;
import static repastcity3.main.ContextManager.LOGGER;
import static repastcity3.utilities.Helper.*;

public class TestEnv {
	/**
	 * Check that the environment looks ok
	 * 
	 * @throws NoIdentifierException
	 */
	public static void testEnvironment(Context<Object> mainContext) throws EnvironmentError, NoIdentifierException {

		LOGGER.log(Level.INFO, "Testing the environment");
		// Get copies of the contexts/projections from main context
		// Context<Building> bc = (Context<Building>)
		// mainContext.getSubContext(GlobalVars.CONTEXT_NAMES.BUILDING_CONTEXT);
		Context<Residential> rc = (Context<Residential>) mainContext
				.getSubContext(GlobalVars.CONTEXT_NAMES.RESIDENTIAL_CONTEXT);
		Context<Road> roc = (Context<Road>) mainContext.getSubContext(GlobalVars.CONTEXT_NAMES.ROAD_CONTEXT);
		Context<Junction> jc = (Context<Junction>) mainContext.getSubContext(GlobalVars.CONTEXT_NAMES.JUNCTION_CONTEXT);

		// Geography<Building> bg = (Geography<Building>)
		// bc.getProjection(GlobalVars.CONTEXT_NAMES.BUILDING_GEOGRAPHY);
		// Geography<Road> rg = (Geography<Road>)
		// rc.getProjection(GlobalVars.CONTEXT_NAMES.ROAD_GEOGRAPHY);
		// Geography<Junction> jg = (Geography<Junction>)
		// rc.getProjection(GlobalVars.CONTEXT_NAMES.JUNCTION_GEOGRAPHY);
		Network<Junction> rn = (Network<Junction>) jc.getProjection(GlobalVars.CONTEXT_NAMES.ROAD_NETWORK);
		System.out.print("roadNetwork has " + rn.size() + "edges\n");

		// 1. Check that there are some objects in each of the contexts
		checkSize(rc, roc, jc);
		// System.out.print("Size is OK!");
		// 2. Check that the number of roads matches the number of edges

		if (sizeOfIterable(roc.getObjects(Road.class)) != sizeOfIterable(rn.getEdges())) {
			throw new EnvironmentError("There should be equal numbers of roads in the road "
					+ "context and edges in the road network. But there are "
					+ sizeOfIterable(roc.getObjects(Road.class)) + " and " + sizeOfIterable(rn.getEdges()));
		}

		// 3. Check that the number of junctions matches the number of nodes
		if (sizeOfIterable(jc.getObjects(Junction.class)) != sizeOfIterable(rn.getNodes())) {
			throw new EnvironmentError("There should be equal numbers of junctions in the junction "
					+ "context and nodes in the road network. But there are "
					+ sizeOfIterable(jc.getObjects(Junction.class)) + " and " + sizeOfIterable(rn.getNodes()));
		}

		LOGGER.log(Level.INFO, "The road network has " + sizeOfIterable(rn.getNodes()) + " nodes and "
				+ sizeOfIterable(rn.getEdges()) + " edges.");

		// 4. Check that Roads and Buildings have unique identifiers
		HashMap<String, ?> idList = new HashMap<String, Object>();
		for (Residential b : rc.getObjects(Residential.class)) {
			if (idList.containsKey(b.getIdentifier()))
				throw new EnvironmentError("More than one residential found with id " + b.getIdentifier());
			idList.put(b.getIdentifier(), null);
		}
		idList.clear();

		for (Road b : roc.getObjects(Road.class)) {
			if (idList.containsKey(b.getIdentifier()))
				throw new EnvironmentError("More than one road found with id " + b.getIdentifier());
			idList.put(b.getIdentifier(), null);
		}
		idList.clear();

	}

}
