/**
 * 
 */
package repastcity3.environment.contexts;

import repast.simphony.context.DefaultContext;
import repastcity3.environment.Residential;
import repastcity3.main.GlobalVars;

/**
 * @author CHAO LUO
 *
 */
public class ResidentialContext extends DefaultContext<Residential>{
	
	public ResidentialContext() {
		super(GlobalVars.CONTEXT_NAMES.RESIDENTIAL_CONTEXT);
	}

}
