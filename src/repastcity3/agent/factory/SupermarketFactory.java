package repastcity3.agent.factory;

import java.util.logging.Logger;

import org.geotools.factory.Hints;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import cern.jet.random.Uniform;
import repast.simphony.random.RandomHelper;
import repastcity3.agent.Farm;
import repastcity3.agent.Supermarket;
import repastcity3.environment.GISFunctions;
import repastcity3.exceptions.AgentCreationException;
import repastcity3.main.AgentControl;
import repastcity3.main.ContextManager;
import repastcity3.main.GlobalVars;

public class SupermarketFactory {
	private static Logger LOGGER = Logger.getLogger(SupermarketFactory.class.getName());

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
		Class<Supermarket> clazz = Supermarket.class;
		fileName = ContextManager.getProperty(GlobalVars.GISDataDirectory)
				+ ContextManager.getProperty(GlobalVars.SupermarketShapefile);

		try {
			GISFunctions.readAgentShapefile(clazz, fileName, ContextManager.supermarketProjection, ContextManager.supermarketContext);
		} catch (Exception e) {
			throw new AgentCreationException(e);
		}


		int numAgents = AgentControl.getSupermarketAgents().size();

		LOGGER.info("Have created " + numAgents + " of type " + clazz.getName().toString() + " from file " + fileName);

	}

	private void createRandomAgents(int num) throws AgentCreationException {
		Uniform nRand = RandomHelper.getUniform();
		for (int i = 0; i < num; i++) {
			Supermarket supermarket=new Supermarket();
			AgentControl.addSupermarketToContext(supermarket);
		}
		Hints hints = new Hints( Hints.CRS, DefaultGeographicCRS.WGS84 );
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
		for (Supermarket supermarket:AgentControl.getSupermarketAgents()) {
			double longitude=nRand.nextDoubleFromTo(114.1450, 133.8333);
			double latitude=nRand.nextDoubleFromTo(22.6362, 22.6087);
			Point p = geometryFactory.createPoint(new Coordinate(longitude, latitude));
			System.out.println("supermarket loc:" + latitude + " " + longitude);
			ContextManager.supermarketProjection.move(supermarket, p);
		}
	}
}
