package repastcity3.agent.factory;

import java.util.logging.Logger;

import repastcity3.agent.Supermarket;
import repastcity3.environment.GISFunctions;
import repastcity3.exceptions.AgentCreationException;
import repastcity3.main.AgentControl;
import repastcity3.main.ContextManager;
import repastcity3.main.GlobalVars;

public class SupermarketFactory {
	private static Logger LOGGER = Logger.getLogger(SupermarketFactory.class.getName());

	public void createAgents() throws AgentCreationException {
		createPointAgents();
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

	private void createRandomAgents() throws AgentCreationException {

	}
}
