/**
 * 
 */
package repastcity3.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

import repast.simphony.engine.schedule.ScheduledMethod;
import repastcity3.agent.IAgent;
import repastcity3.environment.food.DefaultFoodStock;
import repastcity3.environment.food.Food;
import repastcity3.environment.food.FoodOrder;
import repastcity3.environment.food.Nutrition;
import repastcity3.exceptions.NoIdentifierException;

/**
 * @author CHAO LUO
 *
 */
public class Farm extends FarmableLocation implements FixedGeography {

	/** A list of agents who stay here */

	private List<IAgent> agents;

	/** A identifier for this Farm */

	private String identifier;

	/**
	 * The coordinates of the Farm. This is also stored by the projection that
	 * contains this Building but it is useful to have it here too. As they will
	 * never change (buildings don't move) we don't need to worry about keeping them
	 * in sync with the projection.
	 */

	private Coordinate coords;

	// amount of all food
	private double count;

	

	public Farm() {
		//double setupCost,double dailyMaintenanceCost, double fund,List<Food> stock
		super(1000, 100, 10000, new ArrayList<Food>());
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

	private void addFood(Food food) {
		this.stock.add(food);
		this.count += food.getAmount();
	}

	@Override
	public Coordinate getCoords() {
		return this.coords;
	}

	@Override
	public void setCoords(Coordinate c) {
		this.coords = c;

	}

	public String getIdentifier() throws NoIdentifierException {
		if (this.identifier == null) {
			throw new NoIdentifierException("This Farm has no identifier. This can happen "
					+ "when roads are not initialised correctly (e.g. there is no attribute "
					+ "called 'identifier' present in the shapefile used to create this Road)");
		} else {
			return identifier;
		}
	}
	public double getCount() {
		return count;
	}

	public void setCount(double count) {
		this.count = count;
	}

	public void setIdentifier(String id) {
		this.identifier = id;
	}

	public void addAgent(IAgent a) {
		this.agents.add(a);
	}

	public List<IAgent> getAgents() {
		return this.agents;
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

	/**
	 * Return this Farm unique id number.
	 */
	@Override
	public int hashCode() {
		return this.identifier.hashCode();
	}

	public List<Food> getStock() {
		return stock;
	}

	@ScheduledMethod(start = 0, interval = 1)
	@Override
	public synchronized void product() {
		/*
		 * TODO: use strategy for production (use preference list)
		 */

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
	}

}