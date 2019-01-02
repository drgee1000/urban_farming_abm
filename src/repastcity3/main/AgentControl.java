package repastcity3.main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import cern.colt.matrix.doublealgo.Statistic;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.space.gis.Geography;
import repast.simphony.util.collections.IndexedIterable;
import repast.simphony.util.collections.ListIndexedIterable;
import repastcity3.agent.Consumer;
import repastcity3.agent.Farm;
import repastcity3.agent.IAgent;
import repastcity3.agent.Supermarket;
import repastcity3.environment.Building;
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
	public static synchronized void moveAgentByVector(IAgent agent, double distToTravel, double angle) {
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
	public static synchronized void moveAgent(IAgent agent, Point point) {
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
	public static void addConsumerToContext(Consumer agent) {
		ContextManager.agentContext.add(agent);
	}
	
	public static void addFarmToContext(Farm farm)
	{
		ContextManager.agentContext.add(farm);
	}
	
	public static void addSupermarketToContext(Supermarket supermarket)
	{
		ContextManager.agentContext.add(supermarket);
	}
	
	public static Residential getRandomResidential()
	{
		Iterator<Building> tmpList = ContextManager.buildingContext.getRandomObjects(Residential.class, 1)
				.iterator();
		if (tmpList.hasNext()) {
			Residential b = (Residential) tmpList.next();
			return b;
		}else
		{
			return null;
		}
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
	public static IndexedIterable<Consumer> getConsumerAgents() {
		List<Consumer> rawList = new ArrayList<Consumer>();
		for(IAgent consumer:ContextManager.agentContext.getObjects(Consumer.class))
		{
			rawList.add((Consumer)consumer);
		}
		IndexedIterable<Consumer> list=new ListIndexedIterable<>(rawList);
		return list;
	}

	public static IndexedIterable<Farm> getFarmAgents() {
		List<Farm> rawList = new ArrayList<Farm>();
		for(IAgent farm:ContextManager.agentContext.getObjects(Farm.class))
		{
			rawList.add((Farm)farm);
		}
		IndexedIterable<Farm> list=new ListIndexedIterable<>(rawList);
		return list;
	}

	public static IndexedIterable<Supermarket> getSupermarketAgents() {
		List<Supermarket> rawList = new ArrayList<Supermarket>();
		for(IAgent supermarket:ContextManager.agentContext.getObjects(Supermarket.class))
		{
			rawList.add((Supermarket)supermarket);
		}
		IndexedIterable<Supermarket> list=new ListIndexedIterable<>(rawList);
		return list;
	}

	
	/**
	 * Get the geometry of the given agent. This method is required -- rather than
	 * giving agents direct access to the agentGeography -- because when multiple
	 * threads are used they can interfere with each other and agents end up moving
	 * incorrectly.
	 */
	public static Geometry getAgentGeometry(IAgent agent) {
		return ContextManager.agentGeography.getGeometry(agent);
	}
	


}
