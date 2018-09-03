/**
 * 
 */
package repastcity3.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

import com.vividsolutions.jts.geom.Coordinate;

import repast.simphony.engine.schedule.ScheduledMethod;
import repastcity3.agent.IAgent;
import repastcity3.environment.food.DefaultFoodStock;
import repastcity3.environment.food.Food;
import repastcity3.environment.food.FoodOrder;
import repastcity3.environment.food.Nutrition;
import repastcity3.exceptions.NoIdentifierException;
import static repastcity3.main.ContextManager.LOGGER;
/**
 * @author CHAO LUO
 *
 */
public class Farm extends FarmableLocation implements FixedGeography {

	
	// amount of all food
	private double count;

	

	public Farm() {
		//double setupCost,double dailyMaintenanceCost, double fund,List<Food> stock
		super(1000, 100, 10000, new Vector<Food>());
		this.agents = new ArrayList<IAgent>();
		this.count = 0;
		initStock();
	}
	
	

	private void initStock() {
		this.stock = DefaultFoodStock.getRandomFoodList();
		for (Food food : stock) {
			count += food.getAmount();
		}
	}

	
	@Override
	public void step() throws Exception {
		produce();
	}

	@Override
	public boolean isThreadable() {
		return true;
	}
	private void addFood(Food food) {
		this.stock.add(food);
		this.count += food.getAmount();
	}

	public double getCount() {
		return count;
	}

	public void setCount(double count) {
		this.count = count;
	}
	

	@Override
	public String toString() {
		return "Farm: " + this.identifier;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Farm))
			return false;
		Farm b = (Farm) obj;
		return this.identifier.equals(b.identifier);
	}
	
	
	public synchronized void produce() {
		/*
		 * TODO: use strategy for production (use preference list)
		 */
		//LOGGER.log(Level.INFO,"Farm "+this.identifier+" is producting");
		for (Food food : stock) {
			if (fund > 0) {
				double amount = food.getAmount();
				double foodCost = food.getProductionCost();
				double productionAmount = 1 / food.getProductionTime();
				double availableProductionAmount = fund / foodCost;
				double expireAmount = 1 / food.getExpireTime();
				amount -= expireAmount;
				count -= expireAmount;
				if (availableProductionAmount > productionAmount) {
					amount += productionAmount;
					count += productionAmount;
					fund -= foodCost * productionAmount;
				} else {
					amount += availableProductionAmount;
					count += availableProductionAmount;
					fund = 0;
				}
				food.setAmount(amount);

			} else {
				// if there is no fund for production, then stop;
				break;
			}
		}
	}

	public boolean isAvailable() {
		return count > 0;
	}

	public synchronized void sell(FoodOrder order) {
		HashMap<Food, Double> list = order.getList();
		list.forEach((food, amount) -> {
			food.setAmount(food.getAmount() - amount);
			this.fund += amount * food.getPrice();
			count -= amount;
		});
		//let order be collected by GC 
		order=null;
	}
}