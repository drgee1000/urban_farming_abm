package repastcity3.agent;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.referencing.datum.DefaultEllipsoid;

import com.vividsolutions.jts.geom.Coordinate;

import repastcity3.environment.Farm;
import repastcity3.environment.Residential;
import repastcity3.environment.Restaurant;
import repastcity3.environment.Route;
import repastcity3.environment.Shoppingcenter;
import repastcity3.environment.Workplace;
import repastcity3.environment.food.Food;
import repastcity3.environment.food.FoodOrder;
import repastcity3.main.ContextManager;

public class DefaultAgent implements IAgent {

	private static Logger LOGGER = Logger.getLogger(DefaultAgent.class.getName());

	private Residential home; // Where the agent lives
	private Route route; // An object to move the agent around the world

	private Route routedev1;
	private Route routedev2;
	private double routedisogn;
	private double routedisdev1;
	private double routedisdev2;
	private Coordinate origin;
	private Coordinate destination;
	private Farm farm;
	private int flag; // whether it has got enough food in one farm
	
	private boolean goingHome = false; // Whether the agent is going to or from their home
	private boolean goforEat = false;
	private double defaultHealth = 200;
	private double health = defaultHealth;
	private double healthThreshold = 100;
	private double caloryConsumption = 1;
	private double caloryProduction;
	
	private static int uniqueID = 0;
	private final static double p1 = 0.272;
	private final static double p2 = 0.109;
	private final static double p3 = 0.187;
	private final static double p4 = 0.131;
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
//		double generator1 = Math.random();
//		if (generator1 > 0 && generator1 < p1){
//			type = 1; // Workplace
//	
//		} else if (generator1 > p1 && generator1 < (p1 + p2)){
//			type = 2; //Shopping center
//			
//		} else if (generator1 > (p1 + p2) && generator1 < (p1 + p2 + p3)){
//			type = 3; // Restaurant
//			
//		} else {
//			type = 4;
//			
//		}
//		Random generator2 = new Random(new Date().getTime());
//		income = generator2.nextInt(9) + 1;
		return 4;
	}

	@Override
	public void step() throws Exception {
		
		LOGGER.log(Level.FINE, "Agent " + this.id + " is stepping.");
		if(this.health < this.healthThreshold) {
			if(!this.goforEat) {
				this.goforEat = true;
				this.goingHome = false;
				//TO-DO find nearest Farm
				farm = ContextManager.FarmContext.getRandomObject();
				this.route = new Route(this, ContextManager.FarmProjection.getGeometry(farm).getCentroid().getCoordinate(), farm);
				this.origin = ContextManager.getAgentGeometry(this).getCoordinate();
				this.destination = ContextManager.FarmProjection.getGeometry(farm).getCentroid().getCoordinate();
			}
		   if(!this.route.atDestination()) {
				this.route.travel();
				this.health = this.health - caloryConsumption;
			}else if(this.route.atDestination() && flag==0){
				farm = ContextManager.FarmContext.getRandomObject();
				this.route = new Route(this, ContextManager.FarmProjection.getGeometry(farm).getCentroid().getCoordinate(), farm);
				this.origin = ContextManager.getAgentGeometry(this).getCoordinate();
				this.destination = ContextManager.FarmProjection.getGeometry(farm).getCentroid().getCoordinate();
			}else {
				LOGGER.info("Agent" + this.id + " health before eating is" + this.health);
				
				
				if(this.health<this.healthThreshold) {
						this.selectFood(farm);
						if(this.health>this.defaultHealth) {
							flag = 1;
							this.goforEat = false;
							this.route = null;
							setPurpose();
						}else {
							//to-do find farm with most food
							flag = 0;
						}
					}
				
				LOGGER.info("Agent" + this.id + " health after eating is" + this.health);
				
				
			}
		}
		else {	
		switch (type) {
//		case 1: {
//			
//			if (this.route == null) {
//				Workplace w = ContextManager.workplaceContext.getRandomObject();
//				
//				this.route = new Route(this, ContextManager.workplaceProjection.getGeometry(w).getCentroid().getCoordinate(), w);
//				this.origin = ContextManager.getAgentGeometry(this).getCoordinate();
//				this.destination = ContextManager.workplaceProjection.getGeometry(w).getCentroid().getCoordinate();
//				
//			}
//			
//			if (!this.route.atDestination()) {
//				this.route.travel();
//				} else {
//					this.route = null;
//					setPurpose();
//				}
//				break;
//				
//				}
//		
//		case 2: {
//			
//			if (this.route == null) {
//				this.goingHome = false;
//				Shoppingcenter s = ContextManager.shoppingcenterContext.getRandomObject();
//				this.route = new Route(this, ContextManager.shoppingcenterProjection.getGeometry(s).getCentroid().getCoordinate(), s);
//				this.origin = ContextManager.getAgentGeometry(this).getCoordinate();
//				this.destination = ContextManager.shoppingcenterProjection.getGeometry(s).getCentroid().getCoordinate();
//				
//			}
//			
//			if (!this.route.atDestination()) {
//				this.route.travel();
//				
//				
//				} else {
//				
//			
//					this.route = null;
//					setPurpose();
//			}
//		
//			break;
//			
//			
//				}
//		case 3: {
//			
//			if (this.route == null) {
//				this.goingHome = false;
//				Restaurant r = ContextManager.restaurantContext.getRandomObject();
//				this.route = new Route(this, ContextManager.restaurantProjection.getGeometry(r).getCentroid().getCoordinate(), r);
//				this.origin = ContextManager.getAgentGeometry(this).getCoordinate();
//				this.destination = ContextManager.restaurantProjection.getGeometry(r).getCentroid().getCoordinate();
//				
//			}
//			
//			if (!this.route.atDestination()) {
//				this.route.travel();
//				
//				} else {
//					this.route = null;
//					setPurpose();
//				}
//			break;
//		}
		
		case 4: {
			
			if (this.route == null) {
				this.goingHome = false;
				Residential R = ContextManager.residentialContext.getRandomObject();
				while (R == home) {
					R = ContextManager.residentialContext.getRandomObject();
				}
				this.route = new Route(this, ContextManager.residentialProjection.getGeometry(R).getCentroid().getCoordinate(), R);
				this.origin = ContextManager.getAgentGeometry(this).getCoordinate();
				this.destination = ContextManager.residentialProjection.getGeometry(R).getCentroid().getCoordinate();
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
			this.health -= this.health - caloryConsumption;
		}

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
		
//		Iterable<Candidate1> iter_can1 = ContextManager.candidate1Context.getObjects(Candidate1.class);
//		Iterator<Candidate1> iter = iter_can1.iterator();
//		routedisogn = this.route.getShortestPathLength();
//
//		int i = 0;
//		while (iter.hasNext()) {
//			Candidate1 can = iter.next();
//			//System.out.print("iden is " + can.identifier + "\n" );
//			Coordinate transfer = ContextManager.candidate1Projection.getGeometry(can).getCentroid().getCoordinate();
//			this.routedev1 = new Route(this.origin, transfer);
//			this.routedev2 = new Route(transfer, this.destination);
//			this.routedisdev1 = routedev1.myGetDistance();
//			this.routedisdev2 = routedev2.myGetDistance();
//			double temp = (routedisdev1 + routedisdev2 - routedisogn) * 10; 
//			if (temp > 0) {
//				distances[i] = temp;
//			} else {
//				distances[i] = 0;
//			}
//			//System.out.print("distance is " + distances[i] + "\n");
//			i++;
//		}
	}
	
	// Calculate the direct distance from the agent to the candidate locations
	@Override
	public void calDisDes2Can() {
		
//		DefaultEllipsoid e = DefaultEllipsoid.WGS84;
//		Iterable<Candidate1> iter_can1 = ContextManager.candidate1Context.getObjects(Candidate1.class);
//		Iterator<Candidate1> iter = iter_can1.iterator();
//		int i = 0;
//		while (iter.hasNext()) {
//			
//			Candidate1 can = iter.next();
//			Coordinate can_coord = ContextManager.candidate1Projection.getGeometry(can).getCentroid().getCoordinate();
//			distancesDes2Can[i] = e.orthodromicDistance(this.destination.x, this.destination.y, can_coord.x, can_coord.y) / 1000;
//			//System.out.print("dis is " + distancesDes2Can[i] + "\n");
//			i++;
//			
//			
//		}
		
		
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
	
	
	public FoodOrder selectFood(Farm farm) {
		List<Food> stock= farm.getStock();
		FoodOrder foodOrder = new FoodOrder();
		Collections.sort(stock);
		while(health <= defaultHealth && farm.isAvailable()) {
			for(Food f : stock) {
				if(f.getAmount() > 0) {
					foodOrder.addOrder(f,1);
					health += f.getCaboHydrate();
					if(health > defaultHealth)
						break;
				}
			}
		}
		return foodOrder;
	}
	
	
}
