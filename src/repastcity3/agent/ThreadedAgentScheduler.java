
package repastcity3.agent;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import repast.simphony.util.collections.IndexedIterable;
import repastcity3.main.AgentControl;
import repastcity3.main.ContextManager;

public class ThreadedAgentScheduler {

	private static Logger LOGGER = Logger.getLogger(ThreadedAgentScheduler.class.getName());

	private ThreadPoolExecutor poolExecutor;

	public ThreadedAgentScheduler() {
		int numCPUs = Runtime.getRuntime().availableProcessors();
		poolExecutor = new ThreadPoolExecutor(numCPUs, numCPUs, 1L, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(Integer.MAX_VALUE));
	}

	public synchronized void agentStep() {

		agentStep(AgentControl.getFarmAgents());
		agentStep(AgentControl.getSupermarketAgents());
		agentStep(AgentControl.getConsumerAgents());

	}

	
	private void agentStep(IndexedIterable agents)
	{
		final CountDownLatch latch = new CountDownLatch(agents.size());
		for (Object _agent : agents) {
			IAgent agent=(IAgent)_agent;
			AgentTask task = new AgentTask(agent, latch);
			poolExecutor.execute(task);
		}
		try {
//			LOGGER.info("Pool state: [pool queue size]: " + poolExecutor.getQueue().size());
			latch.await();
		} catch (InterruptedException e) {
			LOGGER.log(Level.SEVERE, "agentStep layer error", e);
			ContextManager.stopSim(e, ThreadedAgentScheduler.class);
		} 
	}
	
	
}


class AgentTask implements Runnable {

	private static Logger LOGGER = Logger.getLogger(AgentTask.class.getName());

	private IAgent agent; 
	private CountDownLatch latch;

	public AgentTask(IAgent a, CountDownLatch latch) {
		this.agent = a;
		this.latch = latch;
	}

	@Override
	public void run() {
		try {
//			if(agent instanceof Consumer)
//			{
//				Consumer tmp=(Consumer)agent;
//				System.out.println("current agent_id: "+tmp.getId());
//			}
			this.agent.step();

		} catch (Exception ex) {
			LOGGER.log(Level.SEVERE, "ThreadedAgentScheduler caught an error, telling model to stop", ex);
			ContextManager.stopSim(ex, this.getClass());
		} finally {
			latch.countDown();
		}

	}

}
