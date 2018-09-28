
package repastcity3.agent;

import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.xmlbeans.impl.tool.FactorImports;

import repast.simphony.engine.environment.RunEnvironment;
import repastcity3.main.ContextManager;

/**
 * This class can be used to step agents in different threads simulataneously.
 * If the <code>ContextManager</code> determines that this is a good idea (e.g.
 * if there will be no inter-agent communication) then, rather than using Repast
 * to schedule each agent's step() method directly, it will schedule the
 * agentStep() method (below) instead. This method is then responsible for
 * making the agents step by delegating the work do different threads depending
 * on how many CPU cores are free. As you can imagine, this leads to massive
 * decreases in computation time on multi-core computers.
 * 
 * <p>
 * It is important to note that there will be other side-effects from using
 * multiple threads, particularly agents simultaneously trying to access
 * Building methods or trying to write output data. So care needs to be taken
 * with the rest of the model to prevent problems. The (fairly) naive way that
 * I've tackled this is basically with the liberal use of
 * <code>synchronized</code>
 * </p>
 * 
 * @author Nick Malleson
 * @see ContextManager
 * @see ThreadController
 * @see AgentTask
 */
public class ThreadedAgentScheduler {

	private static Logger LOGGER = Logger.getLogger(ThreadedAgentScheduler.class.getName());

	private boolean burglarsFinishedStepping; // A flag for stepping finish at each iteration

	private  ThreadPoolExecutor poolExecutor;

	private  int numCPUs;

	// private static final CountDownLatch endGate = new CountDownLatch(nThreads);

	public ThreadedAgentScheduler() {
		numCPUs = Runtime.getRuntime().availableProcessors();
		poolExecutor = new ThreadPoolExecutor(numCPUs, numCPUs, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(Integer.MAX_VALUE));
//		LOGGER.severe("=========================numCPUS: "+numCPUs);
	}

	/**
	 * This is called once per iteration and goes through each burglar calling their
	 * step method. This is done (instead of using Repast scheduler) to allow
	 * multi-threading (each step method can be executed on a free core). This
	 * method actually just starts a ThreadController thread (which handles spawning
	 * threads to step burglars) and waits for it to finish
	 */
	public synchronized void agentStep() {
		this.burglarsFinishedStepping = false;
		ArrayList<IAgent> agents = new ArrayList<>(100000);
		for (IAgent consumer : ContextManager.getAllAgents()) {
			agents.add(consumer);
		}
		for (IAgent farm : ContextManager.getFarmAgents()) {
			agents.add(farm);
		}
		Collections.shuffle(agents);
		final CountDownLatch latch = new CountDownLatch(agents.size());

		for (IAgent agent : agents) {
			AgentTask task = new AgentTask(agent, latch);
			poolExecutor.execute(task);
		}

		try {
			LOGGER.severe("Pool state: [pool queue size]: "+poolExecutor.getQueue().size());
			latch.await(); // Wait for the ThreadController to call setBurglarsFinishedStepping().
		} catch (InterruptedException e) {
			LOGGER.log(Level.SEVERE, "", e);
			ContextManager.stopSim(e, ThreadedAgentScheduler.class);
		} // Wait until the thread controller has finished

	}

	/**
	 * Used to tell the ContextCreator that all burglars have finished their step
	 * methods and it can continue doing whatever it was doing (it will be waiting
	 * while burglars are stepping).
	 */
	public synchronized void setBurglarsFinishedStepping() {
		this.burglarsFinishedStepping = true;
		this.notifyAll();
	}
}

/** Single thread to call a Burglar's step method */
class AgentTask implements Runnable {

	private static Logger LOGGER = Logger.getLogger(AgentTask.class.getName());

	private IAgent agent; // The burglar to step
	private CountDownLatch latch;

	public AgentTask(IAgent a, CountDownLatch latch) {
		this.agent = a;
		this.latch = latch;
	}

	@Override
	public void run() {
		try {
			this.agent.step();
			
		} catch (Exception ex) {
			LOGGER.log(Level.SEVERE, "ThreadedAgentScheduler caught an error, telling model to stop", ex);
			ContextManager.stopSim(ex, this.getClass());
		}finally{
			latch.countDown();
		}

	}

}
