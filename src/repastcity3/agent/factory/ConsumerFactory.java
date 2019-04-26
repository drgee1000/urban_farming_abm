
package repastcity3.agent.factory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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
import repastcity3.agent.Consumer;
import repastcity3.agent.Gender;
import repastcity3.environment.GISFunctions;
import repastcity3.environment.Residential;
import repastcity3.exceptions.AgentCreationException;
import repastcity3.main.AgentControl;
import repastcity3.main.ContextManager;
import repastcity3.main.GlobalVars;
import repastcity3.utilities.dataUtility.ConsumerType;
import repastcity3.utilities.dataUtility.SupermarketType;
import repastcity3.utilities.ioUtility.DataLoader;
import repastcity3.utilities.dataUtility.AgentTypeGenerator;

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

			String consumerTypePath = ContextManager.getProperty(GlobalVars.AgentDataDirectory)
					+ContextManager.getProperty(GlobalVars.ConsumerTypeFile);

			Geometry boundary = GISFunctions.readBoundaryShapefile(boundaryFileName);

			GeometryFactory fac = new GeometryFactory();
			// Generate random points in the area to create agents.
			List<Coordinate> agentCoords = GeometryUtil.generateRandomPointsInPolygon(boundary, num);
			ConsumerSimpleFactory consumerFac = new ConsumerSimpleFactory(consumerTypePath);

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

	private Uniform nRand;
	private ArrayList<ConsumerType> consumerTypes;
	private double relativeProbSum;

	public ConsumerSimpleFactory(String consumerTypePath) throws IOException {
		nRand = RandomHelper.getUniform();
		consumerTypes=DataLoader.loadConsumerType(consumerTypePath);
		relativeProbSum=0;
		for(ConsumerType consumerType:consumerTypes)
		{
			relativeProbSum+=consumerType.percentage;
		}
	}

	private ConsumerType getRandomType()
	{
		double prob = nRand.nextDoubleFromTo(0, this.relativeProbSum);
		double tmpSum=0;
		for(ConsumerType consumerType:consumerTypes)
		{
			if(prob<consumerType.percentage+tmpSum)
				return consumerType;
			else 
				tmpSum+=consumerType.percentage;
		}
		return null;
	}

	public Consumer createConsumer() {
		ConsumerType agentData = getRandomType();
		double agentGenderProb = nRand.nextDoubleFromTo(0, 1);
		Gender gender = agentGenderProb <= agentData.mfRatio ? Gender.MALE : Gender.FEMALE;

		String[] food_preference_tmp = agentData.food_preference.split(" ");
		Double[] food_preference = new Double[5];
		int ii = 0;
		for (String fp : food_preference_tmp) {
			food_preference[ii] = Double.valueOf(fp);
			ii++;
		}
		String[] price_preference_tmp = agentData.price_preference.split(" ");
		Double[] price_preference = new Double[5];
		int jj = 0;
		for (String pp : price_preference_tmp) {
			price_preference[jj] = Double.valueOf(pp);
			jj++;
		}
		// System.out.println(price_preference[0]);
		Consumer a = new Consumer(agentData.catagory, gender, agentData.income, agentData.consumption_rate,
				food_preference, price_preference); // Create a new agent
		return a;
	}
}
