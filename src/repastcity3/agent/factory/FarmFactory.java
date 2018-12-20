package repastcity3.agent.factory;

import java.io.IOException;
import java.util.logging.Logger;

import org.antlr.runtime.tree.DoubleLinkTree;
import org.geotools.factory.Hints;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;

import com.l2fprod.common.swing.ComponentFactory.Helper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import cern.jet.random.Uniform;
import it.geosolutions.jaiext.utilities.shape.PolygonIterator;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.GridPoint;
import repastcity3.agent.Farm;
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

	public void createAgents(int num) throws AgentCreationException {
		if(num<0)
		{
			createPointAgents();
		}else {
			createRandomAgents(num);
		}
	}

	private void createPointAgents() throws AgentCreationException {

		String fileName;
		String className;
		Class<Farm> clazz=Farm.class;
		fileName = ContextManager.getProperty(GlobalVars.GISDataDirectory)
				+ ContextManager.getProperty(GlobalVars.FarmShapefile);

		try {
			GISFunctions.readAgentShapefile(clazz, fileName, ContextManager.farmProjection, ContextManager.farmContext);
		} catch (Exception e) {
			throw new AgentCreationException(e);
		}

		for (Farm farm : AgentControl.getFarmAgents()) {
			Geometry geometry = ContextManager.farmProjection.getGeometry(farm);
			Coordinate coordinate = geometry.getCoordinate();

			System.out.println("farm loc:" + coordinate.x + " " + coordinate.y);

		}

		int numAgents = AgentControl.getFarmAgents().size();

		LOGGER.info("Have created " + numAgents + " of type " + clazz.getName().toString() + " from file " + fileName);

	}

	private void createRandomAgents(int num) throws AgentCreationException {
		Uniform nRand = RandomHelper.getUniform();
		for (int i = 0; i <num; i++) {
			Farm farm=new Farm();
			AgentControl.addFarmToContext(farm);
		}
		
		
		Hints hints = new Hints( Hints.CRS, DefaultGeographicCRS.WGS84 );
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(hints);
		for (Farm farm : AgentControl.getFarmAgents()) {
			double longitude=nRand.nextDoubleFromTo(114.1450, 133.8333);
			double latitude=nRand.nextDoubleFromTo(22.6362, 22.6087);
			Point p = geometryFactory.createPoint(new Coordinate(longitude, latitude));
			Coordinate coordinate = p.getCoordinate();
			System.out.println("farm loc:" + coordinate.x + " " + coordinate.y);
			ContextManager.farmProjection.move(farm, p);
			
		}

		
	
	}

}
