package repastcity3.environment;

import java.util.HashMap;
import java.util.List;

import repastcity3.agent.IAgent;
import repastcity3.environment.food.Food;



public abstract class FarmableLocation implements IAgent {
	// for farm data
		protected double setupCost;
		protected double dailyMaintenanceCost;
		protected double fund;
		protected List<Food> stock;
		public abstract void product();
		
		public FarmableLocation(double setupCost, double dailyMaintenanceCost, double fund,
				List<Food> stock) {
			this.setupCost = setupCost;
			this.dailyMaintenanceCost = dailyMaintenanceCost;
			this.fund = fund;
			this.stock = stock;
		}
		
	
		
		
}
