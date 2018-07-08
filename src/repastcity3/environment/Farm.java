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

	// amount of food type that are not zero
	private volatile double count;

	public Farm() {
		this.agents = new ArrayList<IAgent>();
		super.stock = new ArrayList<Food>();
		this.count = 0;
		init();
	}

	private void init() {
		// TODO use more elegant way
		Food potato = new Food("potato", "Staple", 15, 10, 7, new Nutrition(10, 12, 22, 44, 33, 22), 0.25, 25);
		Food rise = new Food("rise", "Staple", 10, 12, 10, new Nutrition(44, 0, 0, 10, 0, 0), 0.3, 100);
		Food cabbage = new Food("cabbage", "vegatable", 12, 23, 17, new Nutrition(10, 3, 0, 10, 15, 5), 0.2, 17);
		Food beaf = new Food("beaf", "meat", 10, 50, 40, new Nutrition(25, 20, 20, 5, 10, 5), 3, 10);
		addFood(potato);
		addFood(rise);
		addFood(cabbage);
		addFood(beaf);
	}

	private void addFood(Food food) {
		this.stock.add(food);
		this.count++;
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
		// TODO Auto-generated method stub
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

	public void sell(FoodOrder order) {
		HashMap<Food, Double> list = order.getList();
		list.forEach((food, amount) -> {
			food.setAmount(food.getAmount() - amount);
			this.fund += amount * food.getPrice();
			if(food.getAmount()<=0)
			{
				this.count--;
			}
		});
	}

}