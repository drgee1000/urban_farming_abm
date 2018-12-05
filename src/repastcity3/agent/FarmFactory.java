package repastcity3.agent;

import java.io.IOException;
import java.util.logging.Logger;

import org.geotools.geometry.jts.ReferencedEnvelope;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import repast.simphony.space.grid.GridPoint;
import repastcity3.environment.GISFunctions;
import repastcity3.environment.Residential;
import repastcity3.environment.SpatialIndexManager;
import repastcity3.exceptions.AgentCreationException;
import repastcity3.main.AgentControl;
import repastcity3.main.ContextManager;
import repastcity3.main.GlobalVars;

public class FarmFactory {

	private static Logger LOGGER = Logger.getLogger(FarmFactory.class.getName());

	public FarmFactory() {

	}

	public void createAgents() throws AgentCreationException {
		createPointAgents();
	}

	private void createPointAgents() throws AgentCreationException {

		String fileName;
		String className;
		Class<Farm> clazz;
		fileName = ContextManager.getProperty(GlobalVars.GISDataDirectory)
				+ ContextManager.getProperty(GlobalVars.FarmShapefile);

		try {
			clazz = Farm.class;
			GISFunctions.readAgentShapefile(clazz, fileName, ContextManager.farmProjection, ContextManager.farmContext);
		} catch (Exception e) {
			throw new AgentCreationException(e);
		}
		
		
		
//		for(Farm farm :AgentControl.getFarmAgents())
//		{
//			Geometry geometry = ContextManager.farmProjection.getGeometry(farm);
//			Coordinate coordinate = geometry.getCoordinate();
////			ContextManager.farmGridProjection.moveTo(farm)
//		}

		int numAgents = AgentControl.getFarmAgents().size();

		LOGGER.info("Have created " + numAgents + " of type " + clazz.getName().toString() + " from file " + fileName);

	}

}