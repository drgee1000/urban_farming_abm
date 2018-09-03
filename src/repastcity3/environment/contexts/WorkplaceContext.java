/**
 * 
 */
package repastcity3.environment.contexts;

import repast.simphony.context.DefaultContext;
import repastcity3.environment.Workplace;
import repastcity3.main.GlobalVars;

/**
 * @author CHAO LUO
 *
 */
public class WorkplaceContext extends DefaultContext<Workplace>{
	
	public WorkplaceContext() {
		super(GlobalVars.CONTEXT_NAMES.WORKPLACE_CONTEXT);
	}

}
