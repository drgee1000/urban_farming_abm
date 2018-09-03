/**
 * 
 */
package repastcity3.environment.contexts;

import repast.simphony.context.DefaultContext;
import repastcity3.main.GlobalVars;
import repastcity3.environment.Shoppingcenter;
/**
 * @author CHAO LUO
 *
 */
public class ShoppingcenterContext extends DefaultContext<Shoppingcenter>{
	
	public ShoppingcenterContext() {
		super(GlobalVars.CONTEXT_NAMES.SHOPPINGCENTER_CONTEXT);
	}

}
