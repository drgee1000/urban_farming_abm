package repastcity3.main;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import cern.colt.matrix.doublealgo.Statistic;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.space.gis.Geography;
import repast.simphony.util.collections.IndexedIterable;
import repastcity3.agent.Consumer;
import repastcity3.agent.Farm;
import repastcity3.agent.IAgent;
import repastcity3.agent.Supermarket;
import repastcity3.environment.Residential;

public class AgentControl {
	/**
	 * Move an agent by a vector. This method is required -- rather than giving
	 * agents direct access to the agentGeography -- because when multiple threads
	 * are used they can interfere with each other and agents end up moving
	 * incorrectly.
	 * 
	 * @param agent        The agent to move.
	 * @param distToTravel The distance that they will travel
	 * @param angle        The angle at which to travel.
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
	 * @param agent The agent to move.
	 * @param point The point to move the agent to
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
	 * @param agent The agent to add.
	 */
	public static synchronized void addConsumerToContext(Consumer agent) {
		ContextManager.agentContext.add(agent);
	}
	
	public static synchronized void addFarmToContext(Farm farm)
	{
		ContextManager.farmContext.add(farm);
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
