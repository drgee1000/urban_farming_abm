
package repastcity3.agent.factory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.geotools.factory.Hints;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import cern.jet.random.Uniform;
import repast.simphony.context.Context;
import repast.simphony.gis.util.GeometryUtil;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.gis.Geography;
import repast.simphony.util.collections.IndexedIterable;
import repastcity3.agent.Consumer;
import repastcity3.agent.Farm;
import repastcity3.agent.Gender;
import repastcity3.agent.Supermarket;
import repastcity3.environment.Building;
import repastcity3.environment.GISFunctions;
import repastcity3.environment.Residential;
import repastcity3.exceptions.AgentCreationException;
import repastcity3.main.AgentControl;
import repastcity3.main.ContextManager;
import repastcity3.main.GlobalVars;

public class ConsumerFactory {

	private static Logger LOGGER = Logger.getLogger(ConsumerFactory.class.getName());

	Context<? super Consumer> context;
	Geography<? super Consumer> geography;

	public ConsumerFactory(Context<? super Consumer> context, Geography<? super Consumer> geography) {
		this.context = context;
		this.geography = geography;
	}

	public void createAgents(int agentNum) throws AgentCreationException {
		createRandomAgents(agentNum);
	}

	private void createRandomAgents(int num) throws AgentCreationException {
		try {
			String boundaryFileName = ContextManager.getProperty(GlobalVars.GISDataDirectory)
					+ ContextManager.getProperty(GlobalVars.CityBoundaryShapefile);

			String agentDataPath = ContextManager.getProperty(GlobalVars.AgentDataPath);

			Geometry boundary = GISFunctions.readBoundaryShapefile(boundaryFileName);

			GeometryFactory fac = new GeometryFactory();
			// Generate random points in the area to create agents.
			List<Coordinate> agentCoords = GeometryUtil.generateRandomPointsInPolygon(boundary, num);
			ConsumerSimpleFactory consumerFac = new ConsumerSimpleFactory(agentDataPath);

			int agentsCreated = 0;
			for (Coordinate coord : agentCoords) {
				Consumer c = consumerFac.createConsumer();
				context.add(c);
				Residential b = AgentControl.getRandomResidential();
				if (b != null) {
					b.addAgent(c);
					c.setHome(b);
				}
				Point geom = fac.createPoint(coord);
				geography.move(c, geom);
				agentsCreated++;
			}
			LOGGER.info("Creating " + agentsCreated + " agents using random method.");

		} catch (IOException e) {
			throw new AgentCreationException("Create Consumer Failed: config file error");
		}

	}

	private void createPointAgents() throws AgentCreationException {

		String fileName;
		String className;
		Class<Consumer> clazz;
		fileName = ContextManager.getProperty(GlobalVars.GISDataDirectory)
				+ ContextManager.getProperty(GlobalVars.ConsumerShapefile);

		try {
			clazz = Consumer.class;
			GISFunctions.readAgentShapefile(clazz, fileName, ContextManager.agentGeography,
					ContextManager.agentContext);
		} catch (Exception e) {
			throw new AgentCreationException(e);
		}

		// Assign agents to houses
		int numAgents = 0;
		for (Consumer a : AgentControl.getConsumerAgents()) {
			numAgents++;
			// TODO: reuse the SpatialIndexManager

			// System.out.print(numAgents + "\n");
//			Geometry g = AgentControl.getAgentGeometry(a);
//			for (Residential b : SpatialIndexManager.search(ContextManager.residentialProjection, g)) {
//				if (ContextManager.residentialProjection.getGeometry(b).contains(g)) {
//					b.addAgent(a);
//					a.setHome(b);
//				}
//			}
			Residential b = AgentControl.getRandomResidential();
			if (b != null) {
				b.addAgent(a);
				a.setHome(b);
			}

		}
		LOGGER.info("Have created " + numAgents + " of type " + clazz.getName().toString() + " from file " + fileName);

	}

}

class ConsumerSimpleFactory {
	private AgentDataGenerator agentDataGenerator;

	private Uniform nRand;

	public ConsumerSimpleFactory(String agentDataPath) throws IOException {
		nRand = RandomHelper.getUniform();
		agentDataGenerator = getAgentDataGenerator(agentDataPath);
	}

	private AgentDataGenerator getAgentDataGenerator(String agentDataPath) throws IOException {

		// read agent config file
		File file = new File(agentDataPath);
		FileReader fileReader = new FileReader(file);
		BufferedReader bReader = new BufferedReader(fileReader);
		Gson gson = new Gson();
		String data = bReader.readLine();
		StringBuffer sb = new StringBuffer();
		while (data != null) {
			sb.append(data);
			data = bReader.readLine();
		}
		List<AgentData> agentDatas = gson.fromJson(sb.toString(), new TypeToken<List<AgentData>>() {
		}.getType());
		int agentTypeSize = agentDatas.size();
		System.out.println("agentTypeSize: " + agentTypeSize);
		AgentDataGenerator agentDataGenerator = new AgentDataGenerator(agentDatas);
		return agentDataGenerator;
	}

	public Consumer createConsumer() {
		double agentTypeProb = nRand.nextDoubleFromTo(0, 1);
		AgentData agentData = agentDataGenerator.getNext();
		double agentGenderProb = nRand.nextDoubleFromTo(0, 1);
		Gender gender = agentGenderProb <= agentData.mfRatio ? Gender.MALE : Gender.FEMALE;
		Consumer a = new Consumer(agentData.catagory, gender); // Create a new agent
		return a;
	}
}
