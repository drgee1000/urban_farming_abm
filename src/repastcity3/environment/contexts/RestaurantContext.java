/**
 * 
 */
package repastcity3.environment.contexts;

import repast.simphony.context.DefaultContext;
import repastcity3.environment.Restaurant;
import repastcity3.main.GlobalVars;

/**
 * @author CHAO LUO
 *
 */
public class RestaurantContext extends DefaultContext<Restaurant>{
	
	public RestaurantContext() {
		super(GlobalVars.CONTEXT_NAMES.RESTAURANT_CONTEXT);
	}

}
