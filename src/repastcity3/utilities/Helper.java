package repastcity3.utilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.parameter.Parameters;
import repastcity3.agent.Farm;
import repastcity3.environment.Route;
import repastcity3.exceptions.EnvironmentError;
import repastcity3.exceptions.ParameterNotFoundException;
import repastcity3.main.ContextManager;

import static repastcity3.main.ContextManager.LOGGER;

public class Helper {
	/**
	 * Checks that the given <code>Context</code>s have more than zero objects in
	 * them
	 * 
	 * @param contexts
	 * @throws EnvironmentError
	 */
	public static void checkSize(Context<?>... contexts) throws EnvironmentError {
		for (Context<?> c : contexts) {
			int numObjs = sizeOfIterable(c.getObjects(Object.class));
			if (numObjs == 0) {
				throw new EnvironmentError("There are no objects in the context: " + c.getId().toString());
			}
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

	public static <E> List<E> iterator2List(Iterator<E> i) {
		List<E> list = new ArrayList<>();
		while (i.hasNext())
			list.add(i.next());
		return list;

		
	}

	public static int getCurrentTick() {
		return (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
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

}
