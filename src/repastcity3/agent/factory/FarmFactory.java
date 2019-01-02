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
import repastcity3.environment.GISFunctions;
import repastcity3.exceptions.AgentCreationException;
import repastcity3.main.AgentControl;
import repastcity3.main.ContextManager;
import repastcity3.main.GlobalVars;

public class FarmFactory {

	private static Logger LOGGER = Logger.getLogger(FarmFactory.class.getName());

	Context<? super Farm> context;
	Geography<? super Farm> geography;

	public FarmFactory(Context<? super Farm> context, Geography<? super Farm> geography) {
		this.context = context;
		this.geography = geography;
	}

	public void createAgents(int num, boolean useArea) throws AgentCreationException {
		if (num < 0) {
			createPointAgents();
		} else {
			createRandomAgents(num,useArea);
		}
	}

	private void createPointAgents() throws AgentCreationException {

		String fileName;
		String className;
		Class<Farm> clazz = Farm.class;
		fileName = ContextManager.getProperty(GlobalVars.GISDataDirectory)
				+ ContextManager.getProperty(GlobalVars.FarmShapefile);

		try {
			GISFunctions.readAgentShapefile(clazz, fileName, geography, context);
		} catch (Exception e) {
			throw new AgentCreationException(e);
		}

		// for debug
		for (Farm farm : AgentControl.getFarmAgents()) {
			Geometry geometry = geography.getGeometry(farm);
			Coordinate coordinate = geometry.getCoordinate();
			System.out.println("farm loc:" + coordinate.x + " " + coordinate.y);
		}
		int numAgents = AgentControl.getFarmAgents().size();
		LOGGER.info("Have created " + numAgents + " of type " + clazz.getName().toString() + " from file " + fileName);

	}

	private void createRandomAgents(int num,boolean useArea) throws AgentCreationException {
		GeometryFactory fac = new GeometryFactory();
		String boundaryFileName;
		List<Coordinate> agentCoords;
		if(useArea)
		{
			boundaryFileName = ContextManager.getProperty(GlobalVars.GISDataDirectory)
					+ ContextManager.getProperty(GlobalVars.FarmBoundaryShapefile);

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
			Farm farm = new Farm();
			AgentControl.addFarmToContext(farm);

			Point geom = fac.createPoint(coord);
			geography.move(farm, geom);
			numAgents++;
		}
		LOGGER.info("Have created " + numAgents + " of type " + Farm.class.getName().toString() + " from boundary file "
				+ boundaryFileName);

	}



}
