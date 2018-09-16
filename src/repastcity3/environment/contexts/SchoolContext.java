package repastcity3.environment.contexts;

import repast.simphony.context.DefaultContext;
import repastcity3.environment.School;
import repastcity3.main.GlobalVars;

public class SchoolContext extends DefaultContext<School> {
	public SchoolContext() {
		super(GlobalVars.CONTEXT_NAMES.SCHOOL_CONTEXT);
	}

}
