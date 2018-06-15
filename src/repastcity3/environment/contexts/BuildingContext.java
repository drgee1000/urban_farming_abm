
package repastcity3.environment.contexts;

import repast.simphony.context.DefaultContext;
import repastcity3.environment.Building;
import repastcity3.main.GlobalVars;


public class BuildingContext extends DefaultContext<Building> {
	
	public BuildingContext() {
		super(GlobalVars.CONTEXT_NAMES.BUILDING_CONTEXT);
	}

}
