/**
 * 
 */
package repastcity3.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

import cern.jet.random.Uniform;
import jdk.nashorn.internal.objects.annotations.Where;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repastcity3.agent.DefaultAgent;
import repastcity3.agent.IAgent;
import repastcity3.environment.food.Food;
import repastcity3.environment.food.FoodConvertor;
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

	private double count;

	public Farm() {
		this.agents = new ArrayList<IAgent>();
		super.stock = new ArrayList<Food>();
		this.count = 0;
		init();
	}

	private void init() {
		// int foodCount = RandomHelper.getUniform().nextInt();
		Food potato = new Food("potato", "Staple", 15, 10, 800,0.2, 0.25, 25);
		stock.add(potato);
		count += 15;
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
	public void product() {
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

	public void sell(DefaultAgent agent) {
		double health = agent.getDefaultHealth() - agent.getHealth();
		double nutrition = FoodConvertor.health2nutrition(health);
		while (nutrition <= 0 && isAvailable()) {
			// TODO random pick
			Food food = stock.get(0);
			double amount = nutrition / food.getNutrition();

			if (food.getAmount() > amount) {
				food.setAmount(food.getAmount() - amount);
				agent.setHealth(agent.getDefaultHealth());
				count -= amount;
			} else {
				food.setAmount(0);
				agent.setHealth(agent.getHealth() + FoodConvertor.nutrition2health(amount * nutrition));
			}
		}

	}

}