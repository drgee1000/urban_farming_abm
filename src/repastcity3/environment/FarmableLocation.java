package repastcity3.environment;

import java.util.HashMap;
import java.util.List;

import repastcity3.environment.food.Food;



public abstract class FarmableLocation {
	// for farm data
		protected double setupCost;
		protected double productionCost;
		protected double dailyMaintenanceCost;
		protected double fund;
		protected List<Food> stock;
		public abstract void product();
}
