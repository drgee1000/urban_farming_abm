package repastcity3.utilities;

import java.util.Iterator;
import java.util.logging.Level;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repastcity3.exceptions.EnvironmentError;
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
	
	public static int getCurrentTick()
	{
		return (int)RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
	}
	
	

	

}
