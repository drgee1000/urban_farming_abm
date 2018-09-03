
package repastcity3.environment.contexts;

import repast.simphony.context.DefaultContext;
import repastcity3.environment.Junction;
import repastcity3.main.GlobalVars;

public class JunctionContext extends DefaultContext<Junction> {
	
	public JunctionContext() {
		super(GlobalVars.CONTEXT_NAMES.JUNCTION_CONTEXT);
	}

}
