/**
 * 
 */
package repastcity3.environment.contexts;

import repast.simphony.context.DefaultContext;
import repastcity3.environment.Substation;
import repastcity3.main.GlobalVars;


/**
 * @author CHAO LUO
 *
 */
public class SubstationContext extends DefaultContext<Substation>{
	
	public SubstationContext() {
		super(GlobalVars.CONTEXT_NAMES.SUBSTATION_CONTEXT);
	}

}
