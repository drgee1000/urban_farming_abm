
package repastcity3.main;

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
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import repast.simphony.space.gis.SimpleAdder;
import repast.simphony.space.graph.Network;
import repastcity3.agent.AgentFactory;
import repastcity3.agent.IAgent;
import repastcity3.agent.ThreadedAgentScheduler;
import repastcity3.environment.Candidate1;
import repastcity3.environment.Candidate2;
import repastcity3.environment.Candidate3;
import repastcity3.environment.GISFunctions;
import repastcity3.environment.Junction;
import repastcity3.environment.NetworkEdgeCreator;
import repastcity3.environment.Residential;
import repastcity3.environment.Restaurant;
import repastcity3.environment.Road;
import repastcity3.environment.Shoppingcenter;
import repastcity3.environment.SpatialIndexManager;
import repastcity3.environment.Substation;
import repastcity3.environment.Farm;
import repastcity3.environment.Workplace;
import repastcity3.environment.contexts.AgentContext;
import repastcity3.environment.contexts.Candidate1Context;
import repastcity3.environment.contexts.Candidate2Context;
import repastcity3.environment.contexts.Candidate3Context;
import repastcity3.environment.contexts.JunctionContext;
import repastcity3.environment.contexts.ResidentialContext;
import repastcity3.environment.contexts.RestaurantContext;
import repastcity3.environment.contexts.RoadContext;
import repastcity3.environment.contexts.ShoppingcenterContext;
import repastcity3.environment.contexts.SubstationContext;
import repastcity3.environment.contexts.FarmContext;
import repastcity3.environment.contexts.WorkplaceContext;
import repastcity3.exceptions.AgentCreationException;
import repastcity3.exceptions.EnvironmentError;
import repastcity3.exceptions.NoIdentifierException;
import repastcity3.exceptions.ParameterNotFoundException;
import repastcity3.main.FarmIO.DataLogger;

public class ContextManager implements ContextBuilder<Object> {

	/*
	 * A logger for this class. Note that there is a static block that is used to configure all logging for the model
	 * (at the bottom of this file).
	 */
	private static Logger LOGGER = Logger.getLogger(ContextManager.class.getName());

	// Optionally force agent threading off (good for debugging)
	private static final boolean TURN_OFF_THREADING = false;

	private static Properties properties;
	
	public static final double MAX_ITERATIONS = 1000;
	
	// Parameters for calculation method
	private static double[] gamma1 = {0.2, 0.05};
	private static double[] gamma2 = {0.15, 0.1};
	private static double[] gamma3 = {-0.2, 0.15};
	
	private static double[] beta1 = {-2.5, -0.15, 1, 1, 1, 1};
	private static double[] beta2 = {-2, -0.13, 1, 1, 1, 1};
	private static double[] beta3 = {-1.5, -0.11, 1, 1, 1, 1};
	
	private static double[] p1 = new double[25]; 
	private static double[] p2 = new double[25];
	private static double[] p3 = new double[25];
	
	private static double c1 = 0.1;
	private static double c2 = 0.2;
	private static double c3 = 0.3;
	
	private static double sigma1 = 0.5;
	private static double sigma2 = 0.5;
	private static double sigma3 = 0.5;
	
	private int numAgent = 600;
	private int numCan = 16;
	private int numCandidate_less = 9;
	private int numItera = 96;
	private double costTranLine = 55900.6;
	
	private double[][] Dis = new double[numAgent][numCan];
	private int[] Res = new int[numCan];
	private int[] Shop = new int[numCan];
	private int[] Super = new int[numCan];
	private int[] TralPurp = new int[numAgent];
	private int[] Income = new int[numAgent];
	private int[][] Des = new int[numAgent][numCan];
	
	private double[] q = new double[numAgent];
	private int[][] index1 = new int[numItera][numCandidate_less];
	private int[][] index2 = new int[numItera][numCandidate_less];
	private int[][] index3 = new int[numItera][numCandidate_less];
	private int[] population = {352711, 352302, 223991, 116360, 229282, 
						122077, 69346, 185439, 188318};
	private double energyPer = 6.721; // in MWh per year per person
	private double[] baseLoad = new double[numCan];
	
	
	private int numCandidate = 16;
	private double cost_lev1 = 40000;
	private double cost_lev2 = 150000;
	private double cost_lev3 = 650000;
	private double[] cost1 = new double[numCandidate];
	private double[] cost2 = new double[numCandidate];
	private double[] cost3 = new double[numCandidate];
	private double a2 = 0.0128;
	private double a1 = 17.82;
	private double a0 = 10.15;

	private double P1 = 0.71;
	private double P2 = 0.96;
	private double P3 = 1.83;
	
	private double[][] Sum = new double[numItera][3];
	
	/*
	 * Pointers to contexts and projections (for convenience). Most of these can be made public, but the agent ones
	 * can't be because multi-threaded agents will simultaneously try to call 'move()' and interfere with each other. So
	 * methods like 'moveAgent()' are provided by ContextManager.
	 */

	private static Context<Object> mainContext;
	
	public static Context<Residential> residentialContext;
	public static Geography<Residential> residentialProjection;
	
	public static Context<Shoppingcenter> shoppingcenterContext;
	public static Geography<Shoppingcenter> shoppingcenterProjection;
	
	public static Context<Farm> FarmContext;
	public static Geography<Farm> FarmProjection;
	
	public static Context<Workplace> workplaceContext;
	public static Geography<Workplace> workplaceProjection;
	
	public static Context<Restaurant> restaurantContext;
	public static Geography<Restaurant> restaurantProjection;

	public static Context<Road> roadContext;
	public static Geography<Road> roadProjection;

	public static Context<Junction> junctionContext;
	public static Geography<Junction> junctionGeography;
	public static Network<Junction> roadNetwork;

	private static Context<IAgent> agentContext;
	private static Geography<IAgent> agentGeography;
	
	public static Context<Candidate1> candidate1Context;
	public static Geography<Candidate1> candidate1Projection;
	
	public static Context<Candidate2> candidate2Context;
	public static Geography<Candidate2> candidate2Projection;
	
	public static Context<Candidate3> candidate3Context;
	public static Geography<Candidate3> candidate3Projection;
	
	public static Context<Substation> substationContext;
	public static Geography<Substation> substationProjection;
	
	
	DataLogger dLogger;
	
	@Override
	public Context<Object> build(Context<Object> con) {

		RepastCityLogging.init();

		// Keep a useful static link to the main context
		mainContext = con;

		// This is the name of the 'root'context
		mainContext.setId(GlobalVars.CONTEXT_NAMES.MAIN_CONTEXT);

		// Read in the model properties
		try {
			readProperties();
		} catch (IOException ex) {
			throw new RuntimeException("Could not read model properties,  reason: " + ex.toString(), ex);
		}

		// Configure the environment
		String gisDataDir = ContextManager.getProperty(GlobalVars.GISDataDirectory);
		LOGGER.log(Level.FINE, "Configuring the environment with data from " + gisDataDir);

		try {
			
			dLogger = new DataLogger();
			// Create the charging station candidate1 (Level 1) - context and geography projection
			candidate1Context = new Candidate1Context();
			candidate1Projection = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
					GlobalVars.CONTEXT_NAMES.CANDIDATE1_GEOGRAPHY, candidate1Context,
					new GeographyParameters<Candidate1>(new SimpleAdder<Candidate1>()));
			String candidate1File = gisDataDir + getProperty(GlobalVars.Candidate1Shapefile);
			GISFunctions.readShapefile(Candidate1.class, candidate1File, candidate1Projection, candidate1Context);
			mainContext.addSubContext(candidate1Context);
			SpatialIndexManager.createIndex(candidate1Projection, Candidate1.class);
			LOGGER.log(Level.FINER, "Read " + candidate1Context.getObjects(Candidate1.class).size() + " charging station candidates (Level 1) from "
					+ candidate1File);
			
			// Create the charging station candidate2 (Level 2) - context and geography projection
			candidate2Context = new Candidate2Context();
			candidate2Projection = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
					GlobalVars.CONTEXT_NAMES.CANDIDATE2_GEOGRAPHY, candidate2Context,
					new GeographyParameters<Candidate2>(new SimpleAdder<Candidate2>()));
			String candidate2File = gisDataDir + getProperty(GlobalVars.Candidate2Shapefile);
			GISFunctions.readShapefile(Candidate2.class, candidate2File, candidate2Projection, candidate2Context);
			mainContext.addSubContext(candidate2Context);
			SpatialIndexManager.createIndex(candidate2Projection, Candidate2.class);
			LOGGER.log(Level.FINER, "Read " + candidate2Context.getObjects(Candidate2.class).size() + " charging station candidates (Level 2) from "
					+ candidate2File);
			
			// Create the charging station candidate3 (Level 3) - context and geography projection
			candidate3Context = new Candidate3Context();
			candidate3Projection = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
					GlobalVars.CONTEXT_NAMES.CANDIDATE3_GEOGRAPHY, candidate3Context,
					new GeographyParameters<Candidate3>(new SimpleAdder<Candidate3>()));
			String candidate3File = gisDataDir + getProperty(GlobalVars.Candidate3Shapefile);
			GISFunctions.readShapefile(Candidate3.class, candidate3File, candidate3Projection, candidate3Context);
			mainContext.addSubContext(candidate3Context);
			SpatialIndexManager.createIndex(candidate3Projection, Candidate3.class);
			LOGGER.log(Level.FINER, "Read " + candidate3Context.getObjects(Candidate3.class).size() + " charging station candidates (Level 3) from "
					+ candidate3File);
			
			
			// Create the substation - context and geography projection
			substationContext = new SubstationContext();
			substationProjection = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
					GlobalVars.CONTEXT_NAMES.SUBSTATION_GEOGRAPHY, substationContext,
					new GeographyParameters<Substation>(new SimpleAdder<Substation>()));
			String substationFile = gisDataDir + getProperty(GlobalVars.SubstationShapefile);
			GISFunctions.readShapefile(Substation.class, substationFile, substationProjection, substationContext);
			mainContext.addSubContext(substationContext);
			SpatialIndexManager.createIndex(substationProjection, Substation.class);
			LOGGER.log(Level.FINER, "Read " + substationContext.getObjects(Substation.class).size() + " substation "
					+ substationFile);
			
			// Create the residential - context and geography projection
			residentialContext = new ResidentialContext();
			residentialProjection = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
					GlobalVars.CONTEXT_NAMES.RESIDENTIAL_GEOGRAPHY, residentialContext,
					new GeographyParameters<Residential>(new SimpleAdder<Residential>()));
			String residentialFile = gisDataDir + getProperty(GlobalVars.ResidentialShapefile);
			GISFunctions.readShapefile(Residential.class, residentialFile, residentialProjection, residentialContext);
			mainContext.addSubContext(residentialContext);
			SpatialIndexManager.createIndex(residentialProjection, Residential.class);
			LOGGER.log(Level.FINER, "Read " + residentialContext.getObjects(Residential.class).size() + " residentials from "
					+ residentialFile);

			
			// Create the shoppingcenter - context and geography projection
			shoppingcenterContext = new ShoppingcenterContext();
			shoppingcenterProjection = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
					GlobalVars.CONTEXT_NAMES.SHOPPINGCENTER_GEOGRAPHY, shoppingcenterContext,
					new GeographyParameters<Shoppingcenter>(new SimpleAdder<Shoppingcenter>()));
			String shoppingcenterFile = gisDataDir + getProperty(GlobalVars.ShoppingcenterShapefile);
			GISFunctions.readShapefile(Shoppingcenter.class, shoppingcenterFile, shoppingcenterProjection, shoppingcenterContext);
			mainContext.addSubContext(shoppingcenterContext);
			SpatialIndexManager.createIndex(shoppingcenterProjection, Shoppingcenter.class);
			LOGGER.log(Level.FINER, "Read " + shoppingcenterContext.getObjects(Shoppingcenter.class).size() + " shoppingcenters from "
					+ shoppingcenterFile);
			
			
			// Create the Farm - context and geography projection
			FarmContext = new FarmContext();
			FarmProjection = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
					GlobalVars.CONTEXT_NAMES.Farm_GEOGRAPHY, FarmContext,
					new GeographyParameters<Farm>(new SimpleAdder<Farm>()));
			String FarmFile = gisDataDir + getProperty(GlobalVars.FarmShapefile);
			GISFunctions.readShapefile(Farm.class, FarmFile, FarmProjection, FarmContext);
			mainContext.addSubContext(FarmContext);
			SpatialIndexManager.createIndex(FarmProjection, Farm.class);
			LOGGER.log(Level.FINER, "Read " + FarmContext.getObjects(Farm.class).size() + " Farms from "
					+ FarmFile);
			
			// Create the workplace - context and geography projection
			workplaceContext = new WorkplaceContext();
			workplaceProjection = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
					GlobalVars.CONTEXT_NAMES.WORKPLACE_GEOGRAPHY, workplaceContext,
					new GeographyParameters<Workplace>(new SimpleAdder<Workplace>()));
			String workplaceFile = gisDataDir + getProperty(GlobalVars.WorkplaceShapefile);
			GISFunctions.readShapefile(Workplace.class, workplaceFile, workplaceProjection, workplaceContext);
			mainContext.addSubContext(workplaceContext);
			SpatialIndexManager.createIndex(workplaceProjection, Workplace.class);
			LOGGER.log(Level.FINER, "Read " + workplaceContext.getObjects(Workplace.class).size() + " workplaces from "
					+ workplaceFile);
			
			// Create the restaurant - context and geography projection
			restaurantContext = new RestaurantContext();
			restaurantProjection = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
					GlobalVars.CONTEXT_NAMES.RESTAURANT_GEOGRAPHY, restaurantContext,
					new GeographyParameters<Restaurant>(new SimpleAdder<Restaurant>()));
			String restaurantFile = gisDataDir + getProperty(GlobalVars.RestaurantShapefile);
			GISFunctions.readShapefile(Restaurant.class, restaurantFile, restaurantProjection, restaurantContext);
			mainContext.addSubContext(restaurantContext);
			SpatialIndexManager.createIndex(restaurantProjection, Restaurant.class);
			LOGGER.log(Level.FINER, "Read " + restaurantContext.getObjects(Restaurant.class).size() + " restaurants from "
					+ restaurantFile);
			
			// Create the Roads - context and geography
			roadContext = new RoadContext();
			roadProjection = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
					GlobalVars.CONTEXT_NAMES.ROAD_GEOGRAPHY, roadContext,
					new GeographyParameters<Road>(new SimpleAdder<Road>()));
			String roadFile = gisDataDir + getProperty(GlobalVars.RoadShapefile);
			GISFunctions.readShapefile(Road.class, roadFile, roadProjection, roadContext);
			mainContext.addSubContext(roadContext);
			SpatialIndexManager.createIndex(roadProjection, Road.class);
			LOGGER.log(Level.FINER, "Read " + roadContext.getObjects(Road.class).size() + " roads from " + roadFile);
			
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
			//
			// Add the junctions to a spatial index (couldn't do this until the
			// road network had been created).
			SpatialIndexManager.createIndex(junctionGeography, Junction.class);

			testEnvironment();

		} catch (MalformedURLException e) {
			LOGGER.log(Level.SEVERE, "", e);
			return null;
		} catch (EnvironmentError e) {
			LOGGER.log(Level.SEVERE, "There is an eror with the environment, cannot start simulation", e);
			return null;
		} catch (NoIdentifierException e) {
			LOGGER.log(Level.SEVERE, "One of the input buildings had no identifier (this should be read"
					+ "from the 'identifier' column in an input GIS file)", e);
			return null;
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, "Could not find an input shapefile to read objects from.", e);
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Now create the agents (note that their step methods are scheduled later
		try {

			agentContext = new AgentContext();
			mainContext.addSubContext(agentContext);
			agentGeography = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
					GlobalVars.CONTEXT_NAMES.AGENT_GEOGRAPHY, agentContext,
					new GeographyParameters<IAgent>(new SimpleAdder<IAgent>()));

			String agentDefn = ContextManager.getParameter(MODEL_PARAMETERS.AGENT_DEFINITION.toString());

			LOGGER.log(Level.INFO, "Creating agents with the agent definition: '" + agentDefn + "'");

			AgentFactory agentFactory = new AgentFactory(agentDefn);
			agentFactory.createAgents(agentContext);

		} catch (ParameterNotFoundException e) {
			LOGGER.log(Level.SEVERE, "Could not find the parameter which defines how agents should be "
					+ "created. The parameter is called " + MODEL_PARAMETERS.AGENT_DEFINITION
					+ " and should be added to the parameters.xml file.", e);
			return null;
		} catch (AgentCreationException e) {
			LOGGER.log(Level.SEVERE, "", e);
			return null;
		}

		// Create the schedule
		createSchedule();

		return mainContext;
	}

	private static <T> List<T> toList(Iterable i) {
		List<T> l = new ArrayList<T>();
		Iterator<T> it = i.iterator();
		while (it.hasNext()) {
			l.add(it.next());
		}
		return l;
	}

	private void createSchedule() {
		RunEnvironment model_core = RunEnvironment.getInstance();
		model_core.endAt(MAX_ITERATIONS);
		ISchedule schedule = model_core.getCurrentSchedule();
		//ScheduleParameters stop = ScheduleParameters.createAtEnd(ScheduleParameters.LAST_PRIORITY);
		//schedule.schedule(stop, this, "calculateStation");
		
		// Schedule something that outputs ticks every 10 iterations.
		schedule.schedule(ScheduleParameters.createRepeating(1, 1, ScheduleParameters.LAST_PRIORITY), this,
				"printTicks");

		/*
		 * Schedule the agents. This is slightly complicated because if all the agents can be stepped at the same time
		 * (i.e. there are no inter- agent communications that make this difficult) then the scheduling is controlled by
		 * a separate function that steps them in different threads. This massively improves performance on multi-core
		 * machines.
		 */
		boolean isThreadable = true;
		for (IAgent a : agentContext.getObjects(IAgent.class)) {
			if (!a.isThreadable()) {
				isThreadable = false;
				break;
			}
		}

		if (ContextManager.TURN_OFF_THREADING) { // Overide threading?
			isThreadable = false;
		}
		
		if (isThreadable && (Runtime.getRuntime().availableProcessors() > 1)) {
			/*
			 * Agents can be threaded so the step scheduling not actually done by repast scheduler, a method in
			 * ThreadedAgentScheduler is called which manually steps each agent.
			 */
			LOGGER.info("The multi-threaded scheduler will be used.");
			ThreadedAgentScheduler s = new ThreadedAgentScheduler();
			ScheduleParameters agentStepParams = ScheduleParameters.createRepeating(1, 1, 0);
			schedule.schedule(agentStepParams, s, "agentStep");
		} else { // Agents will execute in serial, use the repast scheduler.
			LOGGER.log(Level.FINE, "The single-threaded scheduler will be used.");
			ScheduleParameters agentStepParams = ScheduleParameters.createRepeating(1, 1, 0);
			// Schedule the agents' step methods.
			for (IAgent a : agentContext.getObjects(IAgent.class)) {
				schedule.schedule(agentStepParams, a, "step");
			}
		}
	}

	public void printTicks() {
		LOGGER.info("Iterations: " + RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
		
		try{
			
			dLogger.printData(agentContext.getObjects(IAgent.class));
			dLogger.printData(FarmContext.getObjects(Farm.class));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	
	

	/**
	 * Convenience function to get a Simphony parameter
	 * 
	 * @param <T>
	 *            The type of the parameter
	 * @param paramName
	 *            The name of the parameter
	 * @return The parameter.
	 * @throws ParameterNotFoundException
	 *             If the parameter could not be found.
	 */
	public static <V> V getParameter(String paramName) throws ParameterNotFoundException {
		Parameters p = RunEnvironment.getInstance().getParameters();
		Object val = p.getValue(paramName);

		if (val == null) {
			throw new ParameterNotFoundException(paramName);
		}

		// Try to cast the value and return it
		@SuppressWarnings("unchecked")
		V value = (V) val;
		return value;
	}

	/**
	 * Get the value of a property in the properties file. If the input is empty or null or if there is no property with
	 * a matching name, throw a RuntimeException.
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
	 * Read the properties file and add properties. Will check if any properties have been included on the command line
	 * as well as in the properties file, in these cases the entries in the properties file are ignored in preference
	 * for those specified on the command line.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void readProperties() throws FileNotFoundException, IOException {

		File propFile = new File("./repastcity.properties");
		if (!propFile.exists()) {
			throw new FileNotFoundException("Could not find properties file in the default location: "
					+ propFile.getAbsolutePath());
		}

		LOGGER.log(Level.FINE, "Initialising properties from file " + propFile.toString());

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
				LOGGER.log(Level.INFO, "Found a system property '" + k + "->" + newVal
						+ "' which matches a NeissModel property '" + k + "->" + properties.getProperty(k)
						+ "', replacing the non-system one.");
				properties.setProperty(k, newVal);
			}
		} // for
		return;
	} // readProperties

	/**
	 * Check that the environment looks ok
	 * 
	 * @throws NoIdentifierException
	 */
	private void testEnvironment() throws EnvironmentError, NoIdentifierException {

		LOGGER.log(Level.FINE, "Testing the environment");
		// Get copies of the contexts/projections from main context
		//Context<Building> bc = (Context<Building>) mainContext.getSubContext(GlobalVars.CONTEXT_NAMES.BUILDING_CONTEXT);
		Context<Residential> rc = (Context<Residential>) mainContext.getSubContext(GlobalVars.CONTEXT_NAMES.RESIDENTIAL_CONTEXT);
		Context<Workplace> wc = (Context<Workplace>) mainContext.getSubContext(GlobalVars.CONTEXT_NAMES.WORKPLACE_CONTEXT);
		Context<Shoppingcenter> sc = (Context<Shoppingcenter>) mainContext.getSubContext(GlobalVars.CONTEXT_NAMES.SHOPPINGCENTER_CONTEXT);
		Context<Restaurant> Rc = (Context<Restaurant>) mainContext.getSubContext(GlobalVars.CONTEXT_NAMES.RESTAURANT_CONTEXT);
		Context<Road> roc = (Context<Road>) mainContext.getSubContext(GlobalVars.CONTEXT_NAMES.ROAD_CONTEXT);
		Context<Junction> jc = (Context<Junction>) mainContext.getSubContext(GlobalVars.CONTEXT_NAMES.JUNCTION_CONTEXT);

		// Geography<Building> bg = (Geography<Building>)
		// bc.getProjection(GlobalVars.CONTEXT_NAMES.BUILDING_GEOGRAPHY);
		// Geography<Road> rg = (Geography<Road>)
		// rc.getProjection(GlobalVars.CONTEXT_NAMES.ROAD_GEOGRAPHY);
		// Geography<Junction> jg = (Geography<Junction>)
		// rc.getProjection(GlobalVars.CONTEXT_NAMES.JUNCTION_GEOGRAPHY);
		Network<Junction> rn = (Network<Junction>) jc.getProjection(GlobalVars.CONTEXT_NAMES.ROAD_NETWORK);
		System.out.print("roadNetwork has" + rn.size() + "edges\n");
		// 1. Check that there are some objects in each of the contexts
		checkSize(rc, wc, sc, Rc, roc, jc);
		//System.out.print("Size is OK!");
		// 2. Check that the number of roads matches the number of edges
		
		if (sizeOfIterable(roc.getObjects(Road.class)) != sizeOfIterable(rn.getEdges())) {
			throw new EnvironmentError("There should be equal numbers of roads in the road "
					+ "context and edges in the road network. But there are "
					+ sizeOfIterable(roc.getObjects(Road.class)) + " and " + sizeOfIterable(rn.getEdges()));
		}

		// 3. Check that the number of junctions matches the number of nodes
		if (sizeOfIterable(jc.getObjects(Junction.class)) != sizeOfIterable(rn.getNodes())) {
			throw new EnvironmentError("There should be equal numbers of junctions in the junction "
					+ "context and nodes in the road network. But there are "
					+ sizeOfIterable(jc.getObjects(Junction.class)) + " and " + sizeOfIterable(rn.getNodes()));
		}

		LOGGER.log(Level.FINE, "The road network has " + sizeOfIterable(rn.getNodes()) + " nodes and "
				+ sizeOfIterable(rn.getEdges()) + " edges.");

		// 4. Check that Roads and Buildings have unique identifiers
		HashMap<String, ?> idList = new HashMap<String, Object>();
		for (Residential b : rc.getObjects(Residential.class)) {
			if (idList.containsKey(b.getIdentifier()))
				throw new EnvironmentError("More than one residential found with id " + b.getIdentifier());
			idList.put(b.getIdentifier(), null);
		}
		idList.clear();
		
		for (Workplace b : wc.getObjects(Workplace.class)) {
			if (idList.containsKey(b.getIdentifier()))
				throw new EnvironmentError("More than one workplace found with id " + b.getIdentifier());
			idList.put(b.getIdentifier(), null);
		}
		idList.clear();
		
		for (Shoppingcenter b : sc.getObjects(Shoppingcenter.class)) {
			if (idList.containsKey(b.getIdentifier()))
				throw new EnvironmentError("More than one shoppingcenter found with id " + b.getIdentifier());
			idList.put(b.getIdentifier(), null);
		}
		idList.clear();
		
		for (Restaurant b : Rc.getObjects(Restaurant.class)) {
			if (idList.containsKey(b.getIdentifier()))
				throw new EnvironmentError("More than one restaurant found with id " + b.getIdentifier());
			idList.put(b.getIdentifier(), null);
		}
		idList.clear();
		
		for (Road b : roc.getObjects(Road.class)) {
			if (idList.containsKey(b.getIdentifier()))
				throw new EnvironmentError("More than one road found with id " + b.getIdentifier());
			idList.put(b.getIdentifier(), null);
		}

	}

	public static int sizeOfIterable(Iterable i) {
		int size = 0;
		Iterator<Object> it = i.iterator();
		while (it.hasNext()) {
			size++;
			it.next();
		}
		return size;
	}

	/**
	 * Checks that the given <code>Context</code>s have more than zero objects in them
	 * 
	 * @param contexts
	 * @throws EnvironmentError
	 */
	public void checkSize(Context<?>... contexts) throws EnvironmentError {
		for (Context<?> c : contexts) {
			int numObjs = sizeOfIterable(c.getObjects(Object.class));
			if (numObjs == 0) {
				throw new EnvironmentError("There are no objects in the context: " + c.getId().toString());
			}
		}
	}

	public static void stopSim(Exception ex, Class<?> clazz) {
		ISchedule sched = RunEnvironment.getInstance().getCurrentSchedule();
		sched.setFinishing(true);
		sched.executeEndActions();
		LOGGER.log(Level.SEVERE, "ContextManager has been told to stop by " + clazz.getName(), ex);
	}

	/**
	 * Move an agent by a vector. This method is required -- rather than giving agents direct access to the
	 * agentGeography -- because when multiple threads are used they can interfere with each other and agents end up
	 * moving incorrectly.
	 * 
	 * @param agent
	 *            The agent to move.
	 * @param distToTravel
	 *            The distance that they will travel
	 * @param angle
	 *            The angle at which to travel.
	 * @see Geography
	 */
	public static synchronized void moveAgentByVector(IAgent agent, double distToTravel, double angle) {
		ContextManager.agentGeography.moveByVector(agent, distToTravel, angle);
	} //We should use this method!!!!!!

	/**
	 * Move an agent. This method is required -- rather than giving agents direct access to the agentGeography --
	 * because when multiple threads are used they can interfere with each other and agents end up moving incorrectly.
	 * 
	 * @param agent
	 *            The agent to move.
	 * @param point
	 *            The point to move the agent to
	 */
	public static synchronized void moveAgent(IAgent agent, Point point) {
		ContextManager.agentGeography.move(agent, point);
	}

	/**
	 * Add an agent to the agent context. This method is required -- rather than giving agents direct access to the
	 * agentGeography -- because when multiple threads are used they can interfere with each other and agents end up
	 * moving incorrectly.
	 * 
	 * @param agent
	 *            The agent to add.
	 */
	public static synchronized void addAgentToContext(IAgent agent) {
		ContextManager.agentContext.add(agent);
	}

	/**
	 * Get all the agents in the agent context. This method is required -- rather than giving agents direct access to
	 * the agentGeography -- because when multiple threads are used they can interfere with each other and agents end up
	 * moving incorrectly.
	 * 
	 * @return An iterable over all agents, chosen in a random order. See the <code>getRandomObjects</code> function in
	 *         <code>DefaultContext</code>
	 * @see DefaultContext
	 */
	public static synchronized Iterable<IAgent> getAllAgents() {
		return ContextManager.agentContext.getRandomObjects(IAgent.class, ContextManager.agentContext.size());
	}

	/**
	 * Get the geometry of the given agent. This method is required -- rather than giving agents direct access to the
	 * agentGeography -- because when multiple threads are used they can interfere with each other and agents end up
	 * moving incorrectly.
	 */
	public static synchronized Geometry getAgentGeometry(IAgent agent) {
		return ContextManager.agentGeography.getGeometry(agent);
	}

	/**
	 * Get a pointer to the agent context.
	 * 
	 * <p>
	 * Warning: accessing the context directly is not thread safe so this should be used with care. The functions
	 * <code>getAllAgents()</code> and <code>getAgentGeometry()</code> can be used to query the agent context or
	 * projection.
	 * </p>
	 */
	public static Context<IAgent> getAgentContext() {
		return ContextManager.agentContext;
	}

	/**
	 * Get a pointer to the agent geography.
	 * 
	 * <p>
	 * Warning: accessing the context directly is not thread safe so this should be used with care. The functions
	 * <code>getAllAgents()</code> and <code>getAgentGeometry()</code> can be used to query the agent context or
	 * projection.
	 * </p>
	 */
	public static Geography<IAgent> getAgentGeography() {
		return ContextManager.agentGeography;
	}

}

