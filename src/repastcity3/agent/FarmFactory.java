package repastcity3.agent;

import java.io.IOException;
import java.util.logging.Logger;

import org.antlr.runtime.tree.DoubleLinkTree;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;

import com.l2fprod.common.swing.ComponentFactory.Helper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import cern.jet.random.Uniform;
import it.geosolutions.jaiext.utilities.shape.PolygonIterator;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.GridPoint;
import repastcity3.environment.GISFunctions;
import repastcity3.environment.Residential;
import repastcity3.environment.SpatialIndexManager;
import repastcity3.exceptions.AgentCreationException;
import repastcity3.exceptions.NoIdentifierException;
import repastcity3.main.AgentControl;
import repastcity3.main.ContextManager;
import repastcity3.main.GlobalVars;

public class FarmFactory {

	private static Logger LOGGER = Logger.getLogger(FarmFactory.class.getName());

	public FarmFactory() {

	}

	public void createAgents() throws AgentCreationException {
//		createPointAgents();
		createRandomAgents();
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

		for (Farm farm : AgentControl.getFarmAgents()) {
			Geometry geometry = ContextManager.farmProjection.getGeometry(farm);
			Coordinate coordinate = geometry.getCoordinate();

			System.out.println("farm loc:" + coordinate.x + " " + coordinate.y);

//			ContextManager.farmGridProjection.moveTo(farm)
		}

		int numAgents = AgentControl.getFarmAgents().size();

		LOGGER.info("Have created " + numAgents + " of type " + clazz.getName().toString() + " from file " + fileName);

	}

	private void createRandomAgents() throws AgentCreationException {
		Uniform nRand = RandomHelper.getUniform();
		for (int i = 0; i < 10; i++) {
			Farm farm=new Farm();
			AgentControl.addFarmToContext(farm);
			
		}
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
		for (Farm farm : AgentControl.getFarmAgents()) {
			double longitude=nRand.nextDoubleFromTo(114.1450, 133.8333);
			double latitude=nRand.nextDoubleFromTo(22.6362, 22.6087);
			Point p = geometryFactory.createPoint(new Coordinate(latitude, longitude));
			
			System.out.println("farm loc:" + latitude + " " + longitude);
			ContextManager.farmProjection.move(farm, p);
//			ContextManager.farmGridProjection.moveTo(farm)
		}
		
	
	}

}
