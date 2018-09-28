package repastcity3.environment.contexts;

import repast.simphony.context.DefaultContext;
import repastcity3.environment.Supermarket;
import repastcity3.main.GlobalVars;

public class SupermarketContext extends DefaultContext<Supermarket> {

	public SupermarketContext() {
		super(GlobalVars.CONTEXT_NAMES.SUPERMARKET_CONTEXT);
	}
}
