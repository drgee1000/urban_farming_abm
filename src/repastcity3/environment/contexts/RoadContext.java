
package repastcity3.environment.contexts;

import repast.simphony.context.DefaultContext;
import repastcity3.environment.Road;
import repastcity3.main.GlobalVars;


public class RoadContext extends DefaultContext<Road> {
	
	public RoadContext() {
		super(GlobalVars.CONTEXT_NAMES.ROAD_CONTEXT);
	}

}
