package repastcity3.agent;

import java.util.List;

import repastcity3.environment.Farm;
import repastcity3.environment.food.Food;
import repastcity3.environment.Residential;

/**
 * All agents must implement this interface so that it the simulation knows how
 * to step them.
 * 
 * @author Nick Malleson
 * 
 */
public interface IAgent {
	

	/**
	 * Controls the agent. This method will be called by the scheduler once per
	 * iteration.
	 */
	 void step() throws Exception;

	/**
	 * Used by Agents as a means of stating whether or not they can be
	 * run in parallel (i.e. there is no inter-agent communication which will
	 * make parallelisation non-trivial). If all the agents in a simulation
	 * return true, and the computer running the simulation has
	 * more than one core, it is possible to step agents simultaneously.
	 * 
	 * @author Nick Malleson
	 */
	boolean isThreadable();

	
	
//	/**
//	 * (Optional). Add objects to the agents memory. Used to keep a record of all the
//	 * buildings that they have passed.
//	 * @param <T>
//	 * @param objects The objects to add to the memory.
//	 * @param clazz The type of object.
//	 */
//	<T> void addToMemory(List<T> objects, Class<T> clazz);
//	
	



	
	

}