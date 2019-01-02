package repastcity3.agent.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import repast.simphony.context.Context;
import repast.simphony.gis.util.GeometryUtil;
import repast.simphony.space.gis.Geography;
import repastcity3.agent.Farm;
import repastcity3.agent.Supermarket;
import repastcity3.environment.GISFunctions;
import repastcity3.exceptions.AgentCreationException;
import repastcity3.main.AgentControl;
import repastcity3.main.ContextManager;
import repastcity3.main.GlobalVars;

public class SupermarketFactory {
	private static Logger LOGGER = Logger.getLogger(SupermarketFactory.class.getName());

	Context<? super Supermarket> context;
	Geography<? super Supermarket> geography;

	public SupermarketFactory(Context<? super Supermarket> context, Geography<? super Supermarket> geography) {
		this.context = context;
		this.geography = geography;
	}

	public void createAgents(int num,boolean useArea) throws AgentCreationException {
		if (num < 0) {
			createPointAgents();
		} else {
			createRandomAgents(num,useArea);
		}
	}

	private void createPointAgents() throws AgentCreationException {

		String fileName;
		String className;
		Class<Supermarket> clazz = Supermarket.class;
		fileName = ContextManager.getProperty(GlobalVars.GISDataDirectory)
				+ ContextManager.getProperty(GlobalVars.SupermarketShapefile);

		try {
			GISFunctions.readAgentShapefile(clazz, fileName, geography, context);
		} catch (Exception e) {
			throw new AgentCreationException(e);
		}

		int numAgents = AgentControl.getSupermarketAgents().size();

		LOGGER.info("Have created " + numAgents + " of type " + clazz.getName().toString() + " from file " + fileName);

	}

	private void createRandomAgents(int num,boolean useArea) throws AgentCreationException {
		GeometryFactory fac = new GeometryFactory();
		String boundaryFileName;
		List<Coordinate> agentCoords;
		if(useArea)
		{
			boundaryFileName = ContextManager.getProperty(GlobalVars.GISDataDirectory)
					+ ContextManager.getProperty(GlobalVars.SupermarketBoundaryShapefile);
			List<Geometry> boundarys = GISFunctions.readAreaBoundaryShapefile(boundaryFileName);

			double areaSum = 0;
			for (Geometry boundary : boundarys) {
				areaSum += boundary.getArea();
			}

			int numAgents = 0;
			agentCoords = new ArrayList<>();
			for (Geometry boundary : boundarys) {
				int partialNum = (int) (num*(boundary.getArea() / areaSum));
				// Generate random points in the area to create agents.
				List<Coordinate> agentPartialCoords = GeometryUtil.generateRandomPointsInPolygon(boundary, partialNum);
				agentCoords.addAll(agentPartialCoords);
				numAgents += partialNum;
			}
			
			
			if(num-numAgents>0)
			{
				List<Coordinate> agentPartialCoords = GeometryUtil.generateRandomPointsInPolygon(boundarys.get(0), num-numAgents);
				agentCoords.addAll(agentPartialCoords);
			}
		}else
		{
			boundaryFileName = ContextManager.getProperty(GlobalVars.GISDataDirectory)
					+ ContextManager.getProperty(GlobalVars.CityBoundaryShapefile);
			Geometry boundary = GISFunctions.readBoundaryShapefile(boundaryFileName);

			// Generate random points in the area to create agents.
			agentCoords = GeometryUtil.generateRandomPointsInPolygon(boundary, num);
		}
		



		// Create the agents from the collection of random coords.
		int numAgents = 0;
		for (Coordinate coord : agentCoords) {
			Supermarket supermarket = new Supermarket();
			AgentControl.addSupermarketToContext(supermarket);

			Point geom = fac.createPoint(coord);
			geography.move(supermarket, geom);
			numAgents++;
		}
		LOGGER.info("Have created " + numAgents + " of type " + Supermarket.class.getName().toString() + " from boundary file " + boundaryFileName);
	}
}
