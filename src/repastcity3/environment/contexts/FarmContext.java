/**
 * 
 */
package repastcity3.environment.contexts;

import repast.simphony.context.DefaultContext;
import repastcity3.agent.Farm;
import repastcity3.main.GlobalVars;

/**
 * @author CHAO LUO
 *
 */
public class FarmContext extends DefaultContext<Farm>{
	
	public FarmContext() {
		super(GlobalVars.CONTEXT_NAMES.FARM_CONTEXT);
	}

}
