
package repastcity3.environment.contexts;

import repast.simphony.context.DefaultContext;
import repastcity3.agent.IAgent;
import repastcity3.main.GlobalVars;

public class AgentContext extends DefaultContext<IAgent>{
	
	public AgentContext() {
		super(GlobalVars.CONTEXT_NAMES.AGENT_CONTEXT);
	}
	
}
