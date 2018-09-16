package repastcity3.agent;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Coordinate;

import repastcity3.environment.Farm;
import repastcity3.environment.Residential;

import repastcity3.environment.Route;
import repastcity3.environment.School;
import repastcity3.environment.Workplace;
import repastcity3.environment.food.Food;
import repastcity3.environment.food.FoodOrder;
import repastcity3.main.ContextManager;
import repastcity3.utilities.Helper;

public class Consumer implements People {

	private static Logger LOGGER = Logger.getLogger(Consumer.class.getName());

	private Residential home; // Where the agent lives
	private Route route; // An object to move the agent around the world

	private Coordinate origin;
	private Coordinate destination;
	private Farm farm;
	private Residential residential;
	private int flag; // whether it has got enough food in one farm

	private boolean goforEat = false;
	private double defaultHealth = 200;
	private double health = 200;
	private double healthThreshold;
	private double caloryConsumption;
	private double caloryProduction;
	private double mealEnergy;
	private double deathThreshold;

	private static int uniqueID = 0;

	private double thredis = 2; // In kilometer;
	private int id;
	private int type = 3;

	private int income;

	public Consumer() {
		this.id = uniqueID++;
		Random random = new Random();
		this.health = random.nextInt(100)+200;
		this.mealEnergy = random.nextInt(200)+300;
		this.healthThreshold = 0.5*mealEnergy;
		this.deathThreshold = -9*mealEnergy;
		this.caloryConsumption = mealEnergy/50;
		this.residential = ContextManager.residentialContext.getRandomObject();
		setPurpose();
	}

	public int setPurpose() {
		Random random = new Random();
		this.type = random.nextInt(3)+1;
		return type;
	}

	@Override
	public void step() throws Exception {
		LOGGER.log(Level.FINE, "Agent " + this.id + " is stepping.");
		//System.out.println(getHour());
		if (this.health < deathThreshold) {
			LOGGER.log(Level.FINE, "Agent " + this.id + " is dead.");
			ContextManager.dLogger.recordDeath(Helper.getCurrentTick(),this.id);
			ContextManager.getAgentContext().remove(this);
			return;
		}
		if (isEatingTime() && this.health <= healthThreshold) {
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
			}else {
				farm = ContextManager.farmContext.getRandomObject();
				this.route = new Route(this,
						ContextManager.farmProjection.getGeometry(farm).getCentroid().getCoordinate(), farm);
				this.origin = ContextManager.getAgentGeometry(this).getCoordinate();
				this.destination = ContextManager.farmProjection.getGeometry(farm).getCentroid().getCoordinate();
			}
			if (!this.route.atDestination()) {
				this.route.travel();
				
			} else if (this.route.atDestination()) {
				// LOGGER.info("Agent" + this.id + " health before eating is" + this.health);
				FoodOrder foodOrder = this.selectFood(farm);
				
				if (this.health > this.healthThreshold) {
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
			case 1: {
				//System.err.println("enter purpose");
				if (this.route == null) {
					School S = ContextManager.schoolContext.getRandomObject();
					this.route = new Route(this,
							ContextManager.schoolProjection.getGeometry(S).getCentroid().getCoordinate(), S);
					this.origin = ContextManager.getAgentGeometry(this).getCoordinate();
					this.destination = ContextManager.schoolProjection.getGeometry(S).getCentroid().getCoordinate();
				}

				if (!this.route.atDestination()) {
					this.route.travel();

				} else {
					this.route = null;
					setPurpose();
				}

				break;
			}
			case 2: {
				//System.err.println("enter purpose");
				if (this.route == null) {
					Workplace W = ContextManager.workplaceContext.getRandomObject();
					this.route = new Route(this,
							ContextManager.workplaceProjection.getGeometry(W).getCentroid().getCoordinate(), W);
					this.origin = ContextManager.getAgentGeometry(this).getCoordinate();
					this.destination = ContextManager.workplaceProjection.getGeometry(W).getCentroid().getCoordinate();
				}

				if (!this.route.atDestination()) {
					this.route.travel();

				} else {
					this.route = null;
					setPurpose();
				}

				break;
			}
			case 3: {
				
				if (this.route == null) {
					this.route = new Route(this,
							ContextManager.residentialProjection.getGeometry(residential).getCentroid().getCoordinate(), residential);
					this.origin = ContextManager.getAgentGeometry(this).getCoordinate();
					this.destination = ContextManager.residentialProjection.getGeometry(residential).getCentroid().getCoordinate();
				}

				if (!this.route.atDestination()) {
					this.route.travel();

				} else {
					this.route = null;
					setPurpose();
				}

				break;
			}
			

			}

		}
		
		if(isSleepingTime())
			this.health -= caloryConsumption/10;
		else
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
			//System.err.println("enter select food");
			FoodOrder foodOrder = new FoodOrder();
			
			List<Food> stock = farm.getStock();
			int CaloryGet = 0;
			int count = 0;
			for(int i=0; i<stock.size(); ++i) {
				count += stock.get(i).getAmount();
			}
			while ((CaloryGet <= this.mealEnergy||this.health < healthThreshold) && count>0 ) {
				
				count = 0;
				for(int i=0; i<stock.size(); ++i) {
					count += stock.get(i).getAmount();
				}
				//System.err.println(this.id+" "+count);
			
				int len = stock.size();
				//Collections.sort(stock);
				for (int i=0; i<len; i++) {
					Food f = stock.get(i);
					
					if (f.getAmount() > 0) {
						// System.out.println("enter final if");
						foodOrder.addOrder(f, 1);
						f.setAmount(f.getAmount()-1);
						stock.set(i, f);
						// health += f.getCaboHydrate();
						health += f.getCalorie();
						CaloryGet += f.getCalorie();
						
					}
				}
				
			}
			farm.sell(foodOrder);
			return foodOrder;
	}
	
	public boolean isEatingTime() {
		int hour = getHour();
		if((hour>=6 && hour<=8) || (hour>=11 && hour<=13) || (hour>=17 && hour<=19)) {
			return true;
		}else {
			return false;
		}
	}
	public boolean isSleepingTime() {
		int hour = Helper.getCurrentTick();
		if(hour >= 21 || (hour>=0 && hour<=6)) {
			return true;
		}else {
			return false;
		}
	}
	public int getHour() {
		int tick = Helper.getCurrentTick();
		int day = tick/144;
		int hour = (tick-day*144)/6;
		return hour;
	}

}
