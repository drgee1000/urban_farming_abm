
package repastcity3.main;

import static repastcity3.main.ContextManager.LOGGER;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import repast.simphony.space.gis.SimpleAdder;
import repast.simphony.space.graph.Network;
import repast.simphony.util.collections.IndexedIterable;
import repastcity3.agent.AgentFactory;
import repastcity3.agent.Consumer;
import repastcity3.agent.IAgent;
import repastcity3.agent.People;
import repastcity3.agent.ThreadedAgentScheduler;
//import repastcity3.environment.Candidate1;
//import repastcity3.environment.Candidate2;
//import repastcity3.environment.Candidate3;
import repastcity3.environment.GISFunctions;
import repastcity3.environment.Junction;
import repastcity3.environment.NetworkEdgeCreator;
import repastcity3.environment.Residential;
import repastcity3.environment.Restaurant;
import repastcity3.environment.Road;
import repastcity3.environment.School;
import repastcity3.environment.Shoppingcenter;
import repastcity3.environment.SpatialIndexManager;
import repastcity3.environment.Substation;
import repastcity3.environment.Supermarket;
import repastcity3.environment.Farm;
import repastcity3.environment.Workplace;
import repastcity3.environment.contexts.AgentContext;
import repastcity3.environment.contexts.JunctionContext;
import repastcity3.environment.contexts.ResidentialContext;
import repastcity3.environment.contexts.RestaurantContext;
import repastcity3.environment.contexts.RoadContext;
import repastcity3.environment.contexts.SchoolContext;
import repastcity3.environment.contexts.ShoppingcenterContext;
import repastcity3.environment.contexts.SubstationContext;
import repastcity3.environment.contexts.SupermarketContext;
import repastcity3.environment.contexts.FarmContext;
import repastcity3.environment.contexts.WorkplaceContext;
import repastcity3.exceptions.AgentCreationException;
import repastcity3.exceptions.EnvironmentError;
import repastcity3.exceptions.NoIdentifierException;
import repastcity3.exceptions.ParameterNotFoundException;
import repastcity3.exceptions.StockCreationException;
import repastcity3.main.FarmIO.DataLogger;
import repastcity3.utilities.Helper;

public class ContextManager implements ContextBuilder<Object> {

	/*
	 * A logger for this class. Note that there is a static block that is used to
	 * configure all logging for the model (at the bottom of this file).
	 */
	public static Logger LOGGER = Logger.getLogger(ContextManager.class.getName());

	// Optionally force agent threading off (good for debugging)
	private static final boolean TURN_OFF_THREADING = false;

	private static Properties properties;

	public static final double MAX_ITERATIONS = 1000;

	/*
	 * Pointers to contexts and projections (for convenience). Most of these can be
	 * made public, but the agent ones can't be because multi-threaded agents will
	 * simultaneously try to call 'move()' and interfere with each other. So methods
	 * like 'moveAgent()' are provided by ContextManager.
	 */

	private static Context<Object> mainContext;

	public static Context<Residential> residentialContext;
	public static Geography<Residential> residentialProjection;

	public static Context<Farm> farmContext;
	public static Geography<Farm> farmProjection;

	public static Context<School> schoolContext;
	public static Geography<School> schoolProjection;

	public static Context<Workplace> workplaceContext;
	public static Geography<Workplace> workplaceProjection;

	public static Context<Road> roadContext;
	public static Geography<Road> roadProjection;

	public static Context<Supermarket> supermarketContext;
	public static Geography<Supermarket> supermarketProjection;

	public static Context<Junction> junctionContext;
	public static Geography<Junction> junctionGeography;
	public static Network<Junction> roadNetwork;

	private static Context<Consumer> agentContext;
	private static Geography<Consumer> agentGeography;

	public static DataLogger dLogger;

	@Override
	public Context<Object> build(Context<Object> con) {
		// try {
		// dLogger = new DataLogger();
		// } catch (IOException e) {
		// LOGGER.log(Level.SEVERE, "DataLogger create failed", e);
		// }

		RepastCityLogging.init();

		// Keep a useful static link to the main context
		mainContext = con;

		// This is the name of the 'root'context
		mainContext.setId(GlobalVars.CONTEXT_NAMES.MAIN_CONTEXT);

		// Read in the model properties
		readProperties();

		// Configure the environment
		buildEnv();

		// Useless test :(
		// TODO: write some real test
		try {
			TestEnv.testEnvironment(mainContext);
		} catch (EnvironmentError | NoIdentifierException | StockCreationException e) {
			LOGGER.severe("Environment Test failed");
			return null;
		}

		// Now create the agents (note that their step methods are scheduled later
		createAgent();

		// Create the schedule
		createSchedule();

		return mainContext;
	}

	private void buildEnv() {
		try {
			String gisDataDir = ContextManager.getProperty(GlobalVars.GISDataDirectory);
			LOGGER.log(Level.INFO, "Configuring the environment with data from " + gisDataDir);
			// Create the Farm - context and geography projection
			farmContext = new FarmContext();
			farmProjection = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
					GlobalVars.CONTEXT_NAMES.Farm_GEOGRAPHY, farmContext,
					new GeographyParameters<Farm>(new SimpleAdder<Farm>()));
			String FarmFile = gisDataDir + getProperty(GlobalVars.FarmShapefile);
			GISFunctions.readShapefile(Farm.class, FarmFile, farmProjection, farmContext);
			mainContext.addSubContext(farmContext);
			SpatialIndexManager.createIndex(farmProjection, Farm.class);
			LOGGER.log(Level.INFO, "Read " + farmContext.getObjects(Farm.class).size() + "farms from " + FarmFile);

			// Create the supermarket - context and geography projection
			supermarketContext = new SupermarketContext();
			supermarketProjection = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
					GlobalVars.CONTEXT_NAMES.WORKPLACE_GEOGRAPHY, supermarketContext,
					new GeographyParameters<Supermarket>(new SimpleAdder<Supermarket>()));
			String supermarketFile = gisDataDir + getProperty(GlobalVars.SupermarketShapefile);
			GISFunctions.readShapefile(Supermarket.class, supermarketFile, supermarketProjection, supermarketContext);
			mainContext.addSubContext(supermarketContext);
			SpatialIndexManager.createIndex(supermarketProjection, Supermarket.class);
			LOGGER.log(Level.INFO, "Read " + supermarketContext.getObjects(Supermarket.class).size()
					+ " supermarkets from " + supermarketFile);
			// Create the residential - context and geography projection
			residentialContext = new ResidentialContext();
			residentialProjection = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
					GlobalVars.CONTEXT_NAMES.RESIDENTIAL_GEOGRAPHY, residentialContext,
					new GeographyParameters<Residential>(new SimpleAdder<Residential>()));
			String residentialFile = gisDataDir + getProperty(GlobalVars.ResidentialShapefile);
			GISFunctions.readShapefile(Residential.class, residentialFile, residentialProjection, residentialContext);
			mainContext.addSubContext(residentialContext);
			SpatialIndexManager.createIndex(residentialProjection, Residential.class);
			LOGGER.log(Level.INFO, "Read " + residentialContext.getObjects(Residential.class).size()
					+ " residentials from " + residentialFile);

			// Create the school - context and geography projection
			schoolContext = new SchoolContext();
			schoolProjection = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
					GlobalVars.CONTEXT_NAMES.SCHOOL_GEOGRAPHY, schoolContext,
					new GeographyParameters<School>(new SimpleAdder<School>()));
			String schoolFile = gisDataDir + getProperty(GlobalVars.SchoolShapefile);
			GISFunctions.readShapefile(School.class, schoolFile, schoolProjection, schoolContext);
			mainContext.addSubContext(schoolContext);
			SpatialIndexManager.createIndex(schoolProjection, School.class);
			LOGGER.log(Level.INFO,
					"Read " + schoolContext.getObjects(School.class).size() + " schools from " + schoolFile);

			// Create the workplace - context and geography projection
			workplaceContext = new WorkplaceContext();
			workplaceProjection = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
					GlobalVars.CONTEXT_NAMES.WORKPLACE_GEOGRAPHY, workplaceContext,
					new GeographyParameters<Workplace>(new SimpleAdder<Workplace>()));
			String workplaceFile = gisDataDir + getProperty(GlobalVars.WorkplaceShapefile);
			GISFunctions.readShapefile(Workplace.class, workplaceFile, workplaceProjection, workplaceContext);
			mainContext.addSubContext(workplaceContext);
			SpatialIndexManager.createIndex(workplaceProjection, Workplace.class);
			LOGGER.log(Level.INFO, "Read " + workplaceContext.getObjects(Workplace.class).size() + " workplaces from "
					+ workplaceFile);

			// Create
			buildRoad();
		} catch (MalformedURLException e) {
			LOGGER.log(Level.SEVERE, "", e);
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, "Could not find an input shapefile to read builds from.", e);
		}

	}

	private void buildRoad() {
		try {
			String gisDataDir = ContextManager.getProperty(GlobalVars.GISDataDirectory);
			// Create the Roads - context and geography
			roadContext = new RoadContext();
			roadProjection = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
					GlobalVars.CONTEXT_NAMES.ROAD_GEOGRAPHY, roadContext,
					new GeographyParameters<Road>(new SimpleAdder<Road>()));
			String roadFile = gisDataDir + getProperty(GlobalVars.RoadShapefile);

			GISFunctions.readShapefile(Road.class, roadFile, roadProjection, roadContext);

			mainContext.addSubContext(roadContext);
			SpatialIndexManager.createIndex(roadProjection, Road.class);
			LOGGER.log(Level.INFO, "Read " + roadContext.getObjects(Road.class).size() + " roads from " + roadFile);

			// Create road network

			// 1.junctionContext and junctionGeography
			junctionContext = new JunctionContext();
			mainContext.addSubContext(junctionContext);
			junctionGeography = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
					GlobalVars.CONTEXT_NAMES.JUNCTION_GEOGRAPHY, junctionContext,
					new GeographyParameters<Junction>(new SimpleAdder<Junction>()));

			// 2. roadNetwork
			NetworkBuilder<Junction> builder = new NetworkBuilder<Junction>(GlobalVars.CONTEXT_NAMES.ROAD_NETWORK,
					junctionContext, false);
			builder.setEdgeCreator(new NetworkEdgeCreator<Junction>());
			roadNetwork = builder.buildNetwork();
			GISFunctions.buildGISRoadNetwork(roadProjection, junctionContext, junctionGeography, roadNetwork);

			// Add the junctions to a spatial index (couldn't do this until the road network
			// had been created).
			SpatialIndexManager.createIndex(junctionGeography, Junction.class);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoIdentifierException e) {
			LOGGER.log(Level.SEVERE,
					"One of the input roads had no identifier (this should be read from the 'identifier' column in an input GIS file)",
					e);
		}

	}

	private void createAgent() {
		try {
			agentContext = new AgentContext();
			mainContext.addSubContext(agentContext);
			agentGeography = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
					GlobalVars.CONTEXT_NAMES.AGENT_GEOGRAPHY, agentContext,
					new GeographyParameters<Consumer>(new SimpleAdder<Consumer>()));

			int agentNum = Integer.parseInt(Helper.getParameter(MODEL_PARAMETERS.AGENT_NUM.toString()));

			LOGGER.log(Level.INFO, "Creating agents with the agent definition: '" + agentNum + "'");

			AgentFactory agentFactory = new AgentFactory();
			agentFactory.createAgents(agentNum);

		} catch (NumberFormatException e) {
			LOGGER.log(Level.SEVERE, "cannot convert agent_num into an integer.", e);
		} catch (ParameterNotFoundException e) {
			LOGGER.log(Level.SEVERE,
					"Could not find the parameter which defines how agents should be "
							+ "created. The parameter is called " + MODEL_PARAMETERS.AGENT_NUM
							+ " and should be added to the parameters.xml file.",
					e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void createSchedule() {
		RunEnvironment model_core = RunEnvironment.getInstance();
		model_core.endAt(MAX_ITERATIONS);
		ISchedule schedule = model_core.getCurrentSchedule();

		// Schedule that outputs ticks every 10 iterations.
		schedule.schedule(ScheduleParameters.createRepeating(0, 1, ScheduleParameters.LAST_PRIORITY), this,
				"recordTicks");
		schedule.schedule(ScheduleParameters.createAtEnd(ScheduleParameters.LAST_PRIORITY), this, "stopRecord");

		/*
		 * Schedule the agents. This is slightly complicated because if all the agents
		 * can be stepped at the same time (i.e. there are no inter- agent
		 * communications that make this difficult) then the scheduling is controlled by
		 * a separate function that steps them in different threads. This massively
		 * improves performance on multi-core machines.
		 */
		boolean isThreadable = true;
		for (IAgent a : agentContext.getObjects(IAgent.class)) {
			if (!a.isThreadable()) {
				isThreadable = false;
				break;
			}
		}

		if (ContextManager.TURN_OFF_THREADING) {
			isThreadable = false;
		}

		if (isThreadable && (Runtime.getRuntime().availableProcessors() > 1)) {
			/*
			 * Agents can be threaded so the step scheduling not actually done by repast
			 * scheduler, a method in ThreadedAgentScheduler is called which manually steps
			 * each agent.
			 */
			LOGGER.info("The multi-threaded scheduler will be used.");
			ThreadedAgentScheduler s = new ThreadedAgentScheduler();
			ScheduleParameters agentStepParams = ScheduleParameters.createRepeating(1, 1, 10);
			schedule.schedule(agentStepParams, s, "agentStep");
		} else { // Agents will execute in serial, use the repast scheduler.
			LOGGER.log(Level.INFO, "The single-threaded scheduler will be used.");
			// Schedule the agents' step methods.
			for (IAgent a : agentContext.getObjects(IAgent.class)) {
				schedule.schedule(ScheduleParameters.createRepeating(1, 1, 10), a, "step");
			}

			for (IAgent a : farmContext.getObjects(IAgent.class)) {
				schedule.schedule(ScheduleParameters.createRepeating(1, 1, 12), a, "step");
			}

			for (IAgent a : supermarketContext.getObjects(IAgent.class)) {
				schedule.schedule(ScheduleParameters.createRepeating(1, 1, 11), a, "step");
			}
		}
	}

	public void recordTicks() {
		LOGGER.info("Iterations: " + RunEnvironment.getInstance().getCurrentSchedule().getTickCount());

		// try {
		// dLogger.recordData(agentContext.getObjects(IAgent.class),
		// Helper.getCurrentTick());
		// dLogger.recordData(farmContext.getObjects(Farm.class),
		// Helper.getCurrentTick());
		// dLogger.recordData(supermarketContext.getObjects(Supermarket.class),
		// Helper.getCurrentTick());
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	public void stopRecord() {
		try {
			dLogger.stopRecord();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Get the value of a property in the properties file. If the input is empty or
	 * null or if there is no property with a matching name, throw a
	 * RuntimeException.
	 * 
	 * @param property
	 *            The property to look for.
	 * @return A value for the property with the given name.
	 */
	public static String getProperty(String property) {
		if (property == null || property.equals("")) {
			throw new RuntimeException("getProperty() error, input parameter (" + property + ") is "
					+ (property == null ? "null" : "empty"));
		} else {
			String val = ContextManager.properties.getProperty(property);
			if (val == null || val.equals("")) { // No value exists in the
													// properties file
				throw new RuntimeException("checkProperty() error, the required property (" + property + ") is "
						+ (property == null ? "null" : "empty"));
			}
			return val;
		}
	}

	/**
	 * Read the properties file and add properties. Will check if any properties
	 * have been included on the command line as well as in the properties file, in
	 * these cases the entries in the properties file are ignored in preference for
	 * those specified on the command line.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void readProperties() {
		try {
			File propFile = new File("./repastcity.properties");
			if (!propFile.exists()) {
				throw new FileNotFoundException(
						"Could not find properties file in the default location: " + propFile.getAbsolutePath());
			}

			LOGGER.log(Level.INFO, "Initialising properties from file " + propFile.toString());

			ContextManager.properties = new Properties();

			FileInputStream in = new FileInputStream(propFile.getAbsolutePath());
			ContextManager.properties.load(in);
			in.close();

			// See if any properties are being overridden by command-line arguments
			for (Enumeration<?> e = properties.propertyNames(); e.hasMoreElements();) {
				String k = (String) e.nextElement();
				String newVal = System.getProperty(k);
				if (newVal != null) {
					// The system property has the same name as the one from the
					// properties file, replace the one in the properties file.
					LOGGER.log(Level.INFO,
							"Found a system property '" + k + "->" + newVal + "' which matches a NeissModel property '"
									+ k + "->" + properties.getProperty(k) + "', replacing the non-system one.");
					properties.setProperty(k, newVal);
				}
			}

		} catch (IOException e) {
			throw new RuntimeException("Could not read model properties,  reason: " + e.toString(), e);
		}

	}

	public static void stopSim(Exception ex, Class<?> clazz) {
		ISchedule sched = RunEnvironment.getInstance().getCurrentSchedule();
		sched.setFinishing(true);
		sched.executeEndActions();
		LOGGER.log(Level.SEVERE, "ContextManager has been told to stop by " + clazz.getName(), ex);
	}

	/**
	 * Move an agent by a vector. This method is required -- rather than giving
	 * agents direct access to the agentGeography -- because when multiple threads
	 * are used they can interfere with each other and agents end up moving
	 * incorrectly.
	 * 
	 * @param agent
	 *            The agent to move.
	 * @param distToTravel
	 *            The distance that they will travel
	 * @param angle
	 *            The angle at which to travel.
	 * @see Geography
	 */
	public static synchronized void moveAgentByVector(Consumer agent, double distToTravel, double angle) {
		ContextManager.agentGeography.moveByVector(agent, distToTravel, angle);
	} // We should use this method!!!!!!

	/**
	 * Move an agent. This method is required -- rather than giving agents direct
	 * access to the agentGeography -- because when multiple threads are used they
	 * can interfere with each other and agents end up moving incorrectly.
	 * 
	 * @param agent
	 *            The agent to move.
	 * @param point
	 *            The point to move the agent to
	 */
	public static synchronized void moveAgent(Consumer agent, Point point) {
		ContextManager.agentGeography.move(agent, point);
	}

	/**
	 * Add an agent to the agent context. This method is required -- rather than
	 * giving agents direct access to the agentGeography -- because when multiple
	 * threads are used they can interfere with each other and agents end up moving
	 * incorrectly.
	 * 
	 * @param agent
	 *            The agent to add.
	 */
	public static synchronized void addConsumerToContext(Consumer agent) {
		ContextManager.agentContext.add(agent);
	}

	/**
	 * Get all the agents in the agent context. This method is required -- rather
	 * than giving agents direct access to the agentGeography -- because when
	 * multiple threads are used they can interfere with each other and agents end
	 * up moving incorrectly.
	 * 
	 * @return An iterable over all agents, chosen in a random order. See the
	 *         <code>getRandomObjects</code> function in <code>DefaultContext</code>
	 * @see DefaultContext
	 */
	public static synchronized IndexedIterable<Consumer> getConsumerAgents() {
		return ContextManager.agentContext.getObjects(Consumer.class);
	}

	public static synchronized IndexedIterable<Farm> getFarmAgents() {
		return ContextManager.farmContext.getObjects(Farm.class);
	}

	public static synchronized IndexedIterable<Supermarket> getSupermarketAgents() {
		return ContextManager.supermarketContext.getObjects(Supermarket.class);
	}

	public static synchronized IndexedIterable<Residential> getResidentials() {
		return ContextManager.residentialContext.getObjects(Residential.class);
	}

	/**
	 * Get the geometry of the given agent. This method is required -- rather than
	 * giving agents direct access to the agentGeography -- because when multiple
	 * threads are used they can interfere with each other and agents end up moving
	 * incorrectly.
	 */
	public static synchronized Geometry getAgentGeometry(IAgent agent) {
		return ContextManager.agentGeography.getGeometry(agent);
	}

	/**
	 * Get a pointer to the agent context.
	 * 
	 * <p>
	 * Warning: accessing the context directly is not thread safe so this should be
	 * used with care. The functions <code>getAllAgents()</code> and
	 * <code>getAgentGeometry()</code> can be used to query the agent context or
	 * projection.
	 * </p>
	 */
	public static Context<Consumer> getAgentContext() {
		return ContextManager.agentContext;
	}

	/**
	 * Get a pointer to the agent geography.
	 * 
	 * <p>
	 * Warning: accessing the context directly is not thread safe so this should be
	 * used with care. The functions <code>getAllAgents()</code> and
	 * <code>getAgentGeometry()</code> can be used to query the agent context or
	 * projection.
	 * </p>
	 */
	public static Geography<Consumer> getAgentGeography() {
		return ContextManager.agentGeography;
	}

}
