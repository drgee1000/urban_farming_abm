
package repastcity3.agent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vividsolutions.jts.geom.Geometry;

import cern.jet.random.Uniform;
import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import repastcity3.environment.GISFunctions;
import repastcity3.environment.Residential;
import repastcity3.environment.SpatialIndexManager;
import repastcity3.exceptions.AgentCreationException;
import repastcity3.main.ContextManager;
import repastcity3.main.GlobalVars;

public class AgentFactory {

	private static Logger LOGGER = Logger.getLogger(AgentFactory.class.getName());

	private static Gson gson = new Gson();

	private static String agentDataPath = "./data/agent_data/agent.json";

	private static Uniform nRand = RandomHelper.getUniform();

	/** The method to use when creating agents (determined in constructor). */
	private AGENT_FACTORY_METHODS methodToUse;

	/** The definition of the agents - specific to the method being used */
	private String definition;

	/**
	 * Create a new agent factory from the given definition.
	 * 
	 * @param agentDefinition
	 */
	public AgentFactory(String agentDefinition) throws AgentCreationException {

		// First try to parse the definition
		String[] split = agentDefinition.split(":");
		if (split.length != 2) {
			throw new AgentCreationException("Problem parsin the definition string '" + agentDefinition
					+ "': it split into " + split.length + " parts but should split into 2.");
		}
		String method = split[0]; // The method to create agents
		String defn = split[1]; // Information about the agents themselves

		if (method.equals(AGENT_FACTORY_METHODS.RANDOM.toString())) {
			this.methodToUse = AGENT_FACTORY_METHODS.RANDOM;

		} else if (method.equals(AGENT_FACTORY_METHODS.POINT_FILE.toString())) {
			this.methodToUse = AGENT_FACTORY_METHODS.POINT_FILE;
		}

		else if (method.equals(AGENT_FACTORY_METHODS.AREA_FILE.toString())) {
			this.methodToUse = AGENT_FACTORY_METHODS.AREA_FILE;
		}

		else {
			throw new AgentCreationException("Unrecognised method of creating agents: '" + method
					+ "'. Method must be one of " + AGENT_FACTORY_METHODS.RANDOM.toString() + ", "
					+ AGENT_FACTORY_METHODS.POINT_FILE.toString() + " or "
					+ AGENT_FACTORY_METHODS.AREA_FILE.toString());
		}

		this.definition = defn; // Method is OK, save the definition for creating agents later.

		// Check the rest of the definition is also correct (passing false means don't
		// create agents)
		// An exception will be thrown if it doesn't work.
		this.methodToUse.createAgMeth().createagents(false, this);
	}

	public void createAgents(Context<? extends IAgent> context) throws AgentCreationException {
		this.methodToUse.createAgMeth().createagents(true, this);
	}

	/**
	 * Create a number of in randomly chosen houses. If there are more agents than
	 * houses then some houses will have more than one agent in them.
	 * 
	 * @param dummy
	 *            Whether or not to actually create agents. If this is false then
	 *            just check that the definition can be parsed.
	 * @throws AgentCreationException
	 * @throws IOException
	 */
	private void createRandomAgents(boolean dummy) throws AgentCreationException, IOException {
		// Check the definition is as expected, in this case it should be a number
		int numAgents = -1;
		try {
			numAgents = Integer.parseInt(this.definition);
		} catch (NumberFormatException ex) {
			throw new AgentCreationException("Using " + this.methodToUse + " method to create "
					+ "agents but cannot convert " + this.definition + " into an integer.");
		}
		// The definition has been parsed OK, no can either stop or create the agents
		if (dummy) {
			return;
		}

		// read agent config file
		File file = new File(agentDataPath);
		FileReader fileReader = new FileReader(file);
		BufferedReader bReader = new BufferedReader(fileReader);
		String data = bReader.readLine();
		StringBuffer sb = new StringBuffer();
		while (data != null) {
			sb.append(data);
			data = bReader.readLine();
		}
		List<AgentData> agentDatas = gson.fromJson(sb.toString(), new TypeToken<List<AgentData>>() {
		}.getType());
		int agentTypeSize = agentDatas.size();
		System.out.println("agentTypeSize: "+agentTypeSize);
		AgentDataGenerator agentDataGenerator = new AgentDataGenerator(agentDatas);
		// Create agents in randomly chosen houses. Use two while loops in case there
		// are more agents
		// than houses, so that houses have to be looped over twice.
		LOGGER.info("Creating " + numAgents + " agents using " + this.methodToUse + " method.");
		int agentsCreated = 0;
		while (agentsCreated < numAgents) {
			Iterator<Residential> i = ContextManager.residentialContext.getRandomObjects(Residential.class, numAgents)
					.iterator();
			while (i.hasNext() && agentsCreated < numAgents) {
				Residential b = i.next(); // Find a building
				double agentTypeProb = nRand.nextDoubleFromTo(0, 1);
				AgentData agentData = agentDataGenerator.getNext();
				double agentGenderProb = nRand.nextDoubleFromTo(0, 1);
				Gender gender = agentGenderProb <= agentData.mfRatio ? Gender.MALE : Gender.FEMALE;
				Consumer a = new Consumer(agentData.catagory, gender); // Create a new agent

				a.setHome(b); // Tell the agent where it lives
				b.addAgent(a); // Tell the building that the agent lives there
				ContextManager.addAgentToContext(a); // Add the agent to the context
				// Finally move the agent to the place where it lives.
				ContextManager.moveAgent(a, ContextManager.residentialProjection.getGeometry(b).getCentroid());
				agentsCreated++;
			}
		}
	}

	/**
	 * Read a shapefile and create an agent at each location. If there is a column
	 * called
	 * 
	 * @param dummy
	 *            Whether or not to actually create agents. If this is false then
	 *            just check that the definition can be parsed.
	 * @throws AgentCreationException
	 */
	@SuppressWarnings("unchecked")
	private void createPointAgents(boolean dummy) throws AgentCreationException {
		// The definition has been parsed OK, no can either stop or create the agents
		if (dummy) {
			return;
		}
		// See if there is a single type of agent to create or should read a colum in
		// shapefile
		boolean singleType = this.definition.contains("$");

		String fileName;
		String className;
		Class<Consumer> clazz;
		if (singleType) {
			// Agent class provided, can use the Simphony Shapefile loader to load agents of
			// the given class

			// Work out the file and class names from the agent definition
			String[] split = this.definition.split("\\$");
			if (split.length != 2) {
				throw new AgentCreationException("There is a problem with the agent definition, I should be "
						+ "able to split the definition into two parts on '$', but only split it into " + split.length
						+ ". The definition is: '" + this.definition + "'");
			}
			// (Need to append root data directory to the filename).
			fileName = ContextManager.getProperty(GlobalVars.GISDataDirectory) + split[0];
			className = split[1];
			// Try to create a class from the given name.
			try {
				clazz = (Class<Consumer>) Class.forName(className);
				GISFunctions.readAgentShapefile(clazz, fileName, ContextManager.getAgentGeography(),
						ContextManager.getAgentContext());
			} catch (Exception e) {
				throw new AgentCreationException(e);
			}
		} else {
			// TODO Implement agent creation from shapefile value;
			throw new AgentCreationException(
					"Have not implemented the method of reading agent classes from a " + "shapefile yet.");
		}

		// Assign agents to houses
		int numAgents = 0;
		for (Consumer a : ContextManager.getAllAgents()) {
			numAgents++;
			// System.out.print(numAgents + "\n");
			Geometry g = ContextManager.getAgentGeometry(a);
			for (Residential b : SpatialIndexManager.search(ContextManager.residentialProjection, g)) {
				if (ContextManager.residentialProjection.getGeometry(b).contains(g)) {
					b.addAgent(a);
					a.setHome(b);
				}
			}
		}

		if (singleType) {
			LOGGER.info(
					"Have created " + numAgents + " of type " + clazz.getName().toString() + " from file " + fileName);

		} else {
			// (NOTE: at the moment this will never happen because not implemented yet.)
			LOGGER.info("Have created " + numAgents + " of different types from file " + fileName);
		}

	}

	private void createAreaAgents(boolean dummy) throws AgentCreationException {
		throw new AgentCreationException("Have not implemented the createAreaAgents method yet.");
	}

	/**
	 * The methods that can be used to create agents. The CreateAgentMethod stuff is
	 * just a long-winded way of hard-coding the specific method to use for creating
	 * agents into the enum (much simpler in python).
	 * 
	 * @author Nick Malleson
	 */
	private enum AGENT_FACTORY_METHODS {
		/** Default: create a number of agents randomly assigned to buildings */
		RANDOM("random", new CreateAgentMethod() {
			@Override
			public void createagents(boolean b, AgentFactory af) throws AgentCreationException {
				try {
					af.createRandomAgents(b);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}),
		/** Specify an agent shapefile, one agent will be created per point */
		POINT_FILE("point", new CreateAgentMethod() {
			@Override
			public void createagents(boolean b, AgentFactory af) throws AgentCreationException {
				af.createPointAgents(b);
			}
		}),
		/**
		 * Specify the number of agents per area as a shaefile. Agents will be randomly
		 * assigned to houses within the area.
		 */
		AREA_FILE("area", new CreateAgentMethod() {
			@Override
			public void createagents(boolean b, AgentFactory af) throws AgentCreationException {
				af.createAreaAgents(b);
			}
		});

		String stringVal;
		CreateAgentMethod meth;

		/**
		 * @param val
		 *            The string representation of the enum which must match the method
		 *            given in the 'agent_definition' parameter in parameters.xml.
		 * @param f
		 */
		AGENT_FACTORY_METHODS(String val, CreateAgentMethod f) {
			this.stringVal = val;
			this.meth = f;
		}

		public String toString() {
			return this.stringVal;
		}

		public CreateAgentMethod createAgMeth() {
			return this.meth;
		}

		interface CreateAgentMethod {
			void createagents(boolean dummy, AgentFactory af) throws AgentCreationException;
		}
	}

}
