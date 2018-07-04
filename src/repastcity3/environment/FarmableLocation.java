package repastcity3.environment;

import java.util.HashMap;

public abstract class FarmableLocation {
	// for farm data
		protected double setupCost;
		protected double productionCost;
		protected double dailyMaintenanceCost;
		protected double fund;
		protected HashMap<String, Food> stock;

		public abstract void product();
}
