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

public class DefaultAgent implements IAgent {

	private static Logger LOGGER = Logger.getLogger(DefaultAgent.class.getName());

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
	private int MAX = 16;
	private  double[] distances = new double[MAX];
	private double[] distancesDes2Can = new double[MAX];
	
	
	private double thredis = 2; // In kilometer; 
	private int id;
	private int type;
	
	private int income;
	
	
	public DefaultAgent() {
		this.id = uniqueID++;		
		setPurpose();
		
	}
	
	public int setPurpose() {
		return 4;
	}

	@Override
	public void step() throws Exception {
//		System.out.println(this.health);
		LOGGER.log(Level.FINE, "Agent " + this.id + " is stepping.");
		if(this.health < -50) {
			LOGGER.log(Level.FINE, "Agent " + this.id + " is dead.");
			ContextManager.getAgentContext().remove(this);
			return;
		}
		if(this.health < this.healthThreshold) {
			if(!this.goforEat) {
				this.goforEat = true;
				farm = this.findNearestFarm();
				if(!farm.isAvailable()) {
					farm = ContextManager.farmContext.getRandomObject();
				}
				this.route = new Route(this, ContextManager.farmProjection.getGeometry(farm).getCentroid().getCoordinate(), farm);
				this.origin = ContextManager.getAgentGeometry(this).getCoordinate();
				this.destination = ContextManager.farmProjection.getGeometry(farm).getCentroid().getCoordinate();
			}
		   if(!this.route.atDestination()) {
				this.route.travel();
				this.health = this.health - caloryConsumption;
			}else if(this.route.atDestination())
			 	{
				//LOGGER.info("Agent" + this.id + " health before eating is" + this.health);
				FoodOrder foodOrder = this.selectFood(farm);
				farm.sell(foodOrder);
				if(this.health>this.defaultHealth) {
						flag = 1;
						this.goforEat = false;
						this.route = null;
						setPurpose();
					}else {
						this.goforEat = true;
						flag = 0;
					}
					
				
				//LOGGER.info("Agent" + this.id + " health after eating is" + this.health);
				
				
			}
		}
		else {	
		switch (type) {
		case 4: {
			
			if (this.route == null) {
				Farm R = ContextManager.farmContext.getRandomObject();
				
				this.route = new Route(this, ContextManager.farmProjection.getGeometry(R).getCentroid().getCoordinate(), R);
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
	 * There will be no inter-agent communication so these agents can be executed simulataneously in separate threads.
	 */
	@Override
	public final boolean isThreadable() {
		return true;
	}

	@Override
	public void setHome(Residential home) {
		this.home = home;
	}

	@Override
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
		return "Agent " + this.id;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DefaultAgent))
			return false;
		DefaultAgent b = (DefaultAgent) obj;
		return this.id == b.id;
	}

	@Override
	public int hashCode() {
		return this.id;
	}
	
	
	// Calculate the deviating distance 
	@Override
	public void calculateDis(){
	}
	
	// Calculate the direct distance from the agent to the candidate locations
	@Override
	public void calDisDes2Can() {
	}
	
	public Farm findNearestFarm() {
		Iterator<Farm> iter = ContextManager.farmContext.iterator();
		double min = Double.POSITIVE_INFINITY;
		Farm nearestFarm = null;
		//may not iterate all the farms.
		int iterTime = 100;
		while(iter.hasNext()) {
			Farm farm = iter.next();
			Route r = new Route(this, ContextManager.farmProjection.getGeometry(farm).getCentroid().getCoordinate(), farm);
			this.origin = ContextManager.getAgentGeometry(this).getCoordinate();
			this.destination = ContextManager.farmProjection.getGeometry(farm).getCentroid().getCoordinate();
			double dis = (origin.x-destination.x)*(origin.x-destination.x) + (origin.y-destination.y)*(origin.y-destination.y);
			if(dis<min) {
				min = dis;
				nearestFarm =farm;
			}
		}
		return nearestFarm;
	}
	
	public Farm findFarmWithMostFood() {
		Iterator<Farm> iter = ContextManager.farmContext.iterator();
		double max = -1;
		Farm mostFoodFarm = null;
		int iterTime = 100;
		while(iter.hasNext()) {
			Farm farm = iter.next();
			// to-do
			// in the farm class, add a method to return the total amount of food
		}
		return mostFoodFarm;
	}
	@Override
	public double returnDis(String id) {
		
		int iden = Integer.parseInt(id) - 1;
		//System.out.print("iden is " + iden + "\n");
		return distances[iden];
	}	
	
	
	@Override
	public int getIncome() {
		return income;
	}
	
	
	// Check if the charging station is near this agent's destination
	@Override
	public int isDes(String id) {
		
		if (this.type == 5)
			return 1;
		
		int iden = Integer.parseInt(id) - 1;
		//System.out.print("idn is " + iden + "\n");
		if (distancesDes2Can[iden] < thredis)
			return 1;
		else
			return 0;
		
		
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
	
	
	synchronized public FoodOrder selectFood(Farm farm) {
		//System.out.println("enter select food");
		List<Food> stock= farm.getStock();
		FoodOrder foodOrder = new FoodOrder();
		Collections.sort(stock);
		while(health <= defaultHealth && farm.isAvailable()) {
			//System.out.println("enter while loop");
			for(Food f : stock) {
				if(f.getAmount() > 0) {
//					System.out.println("enter final if");
					foodOrder.addOrder(f,1);
					//health += f.getCaboHydrate();
					health += 20;
					if(health > defaultHealth)
						break;
				}
			}
		}
		return foodOrder;
	}
	
	
}
