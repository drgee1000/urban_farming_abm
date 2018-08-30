package repastcity3.agent;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Coordinate;

import repastcity3.environment.Farm;
import repastcity3.environment.Residential;

import repastcity3.environment.Route;

import repastcity3.environment.food.Food;
import repastcity3.environment.food.FoodOrder;
import repastcity3.main.ContextManager;

public class Consumer implements People {

	private static Logger LOGGER = Logger.getLogger(Consumer.class.getName());

	private Residential home; // Where the agent lives
	private Route route; // An object to move the agent around the world

	private Coordinate origin;
	private Coordinate destination;
	private Farm farm;
	private int flag; // whether it has got enough food in one farm

	private boolean goforEat = false;
	private double defaultHealth = 200;
	private double health = 200;
	private double healthThreshold = 100;
	private double caloryConsumption = 5;
	private double caloryProduction;

	private static int uniqueID = 0;

	private double thredis = 2; // In kilometer;
	private int id;
	private int type;

	private int income;

	public Consumer() {
		this.id = uniqueID++;
		setPurpose();

	}

	public int setPurpose() {
		return 4;
	}

	@Override
	public void step() throws Exception {
		// System.out.println(this.health);
		LOGGER.log(Level.FINE, "Agent " + this.id + " is stepping.");
		//System.out.println("step"+this.id);
		if (this.health < -50) {
			LOGGER.log(Level.FINE, "Agent " + this.id + " is dead.");
			ContextManager.getAgentContext().remove(this);
			return;
		}
		if (this.health < this.healthThreshold) {
			if (!this.goforEat) {
				this.goforEat = true;
				farm = this.findNearestFarm();
				if (!farm.isAvailable() || farm.getCount()<50) {
					farm = ContextManager.farmContext.getRandomObject();
				}
				this.route = new Route(this,
						ContextManager.farmProjection.getGeometry(farm).getCentroid().getCoordinate(), farm);
				this.origin = ContextManager.getAgentGeometry(this).getCoordinate();
				this.destination = ContextManager.farmProjection.getGeometry(farm).getCentroid().getCoordinate();
			}
			if (!this.route.atDestination()) {
				this.route.travel();
				this.health = this.health - caloryConsumption;
			} else if (this.route.atDestination()) {
				// LOGGER.info("Agent" + this.id + " health before eating is" + this.health);
				FoodOrder foodOrder = this.selectFood(farm);
				farm.sell(foodOrder);
				if (this.health > this.defaultHealth) {
					flag = 1;
					this.goforEat = false;
					this.route = null;
					setPurpose();
				} else {
					this.goforEat = true;
					flag = 0;
				}

				// LOGGER.info("Agent" + this.id + " health after eating is" + this.health);

			}
		} else {
			switch (type) {
			case 4: {

				if (this.route == null) {
					Farm R = ContextManager.farmContext.getRandomObject();

					this.route = new Route(this,
							ContextManager.farmProjection.getGeometry(R).getCentroid().getCoordinate(), R);
					this.origin = ContextManager.getAgentGeometry(this).getCoordinate();
					this.destination = ContextManager.farmProjection.getGeometry(R).getCentroid().getCoordinate();
				}

				if (!this.route.atDestination()) {
					this.route.travel();
					this.health -= caloryConsumption;

				} else {
					this.route = null;
					setPurpose();
				}

				break;
			}

			}

		}
		this.health -= caloryConsumption;
		
	}

	/**
	 * There will be no inter-agent communication so these agents can be executed
	 * simulataneously in separate threads.
	 */
	@Override
	public final boolean isThreadable() {
		return true;
	}

	public void setHome(Residential home) {
		this.home = home;
	}

	public Residential getHome() {
		return this.home;
	}

	@Override
	public <T> void addToMemory(List<T> objects, Class<T> clazz) {
	}

	@Override
	public List<String> getTransportAvailable() {
		return null;
	}

	@Override
	public String toString() {
		return "Consumer " + this.id;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Consumer))
			return false;
		Consumer b = (Consumer) obj;
		return this.id == b.id;
	}

	@Override
	public int hashCode() {
		return this.id;
	}

	public Farm findNearestFarm() {
		Iterator<Farm> iter = ContextManager.farmContext.iterator();
		double min = Double.POSITIVE_INFINITY;
		Farm nearestFarm = null;
		// may not iterate all the farms.
		int iterTime = 100;
		while (iter.hasNext()) {
			Farm farm = iter.next();
			Route r = new Route(this, ContextManager.farmProjection.getGeometry(farm).getCentroid().getCoordinate(),
					farm);
			this.origin = ContextManager.getAgentGeometry(this).getCoordinate();
			this.destination = ContextManager.farmProjection.getGeometry(farm).getCentroid().getCoordinate();
			double dis = (origin.x - destination.x) * (origin.x - destination.x)
					+ (origin.y - destination.y) * (origin.y - destination.y);
			if (dis < min) {
				min = dis;
				nearestFarm = farm;
			}
		}
		return nearestFarm;
	}

	public Farm findFarmWithMostFood() {
		Iterator<Farm> iter = ContextManager.farmContext.iterator();
		double max = -1;
		Farm mostFoodFarm = null;
		int iterTime = 100;
		while (iter.hasNext()) {
			Farm farm = iter.next();
			// to-do
			// in the farm class, add a method to return the total amount of food
		}
		return mostFoodFarm;
	}

	public int getIncome() {
		return income;
	}

	public double getCaloryConsumption() {
		return caloryConsumption;
	}

	public double getCaloryProduction() {
		return caloryProduction;
	}

	public double getHealth() {
		return this.health;
	}

	public void setHealth(double health) {
		this.health = health;
	}

	public double getHealthThreshold() {
		return this.healthThreshold;
	}

	public void setHealthThreshold(double healthThreshold) {
		this.healthThreshold = healthThreshold;
	}

	public double getDefaultHealth() {
		return defaultHealth;
	}

	public void setDefaultHealth(double defaultHealth) {
		this.defaultHealth = defaultHealth;
	}

	public FoodOrder selectFood(Farm farm) {
			FoodOrder foodOrder = new FoodOrder();
			int iter = 0;
			int maxIter = 50;
			while (health <= defaultHealth && farm.isAvailable() && iter < maxIter) {
				iter++;
				List<Food> stock = farm.getStock();
				int len = stock.size();
				//Collections.sort(stock);
				for (int i=0; i<len; i++) {
					
					Food f = stock.get(i);
					if (f.getAmount() > 0) {
						// System.out.println("enter final if");
						foodOrder.addOrder(f, 1);
						// health += f.getCaboHydrate();
						health += 20;
						if (health > defaultHealth)
							break;
					}
				}
				
			}
			
			return foodOrder;
		
		

	}

}
