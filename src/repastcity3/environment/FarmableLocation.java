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
		public abstract void produce();
		
		public FarmableLocation(double setupCost, double dailyMaintenanceCost, double fund,
				List<Food> stock) {
			this.setupCost = setupCost;
			this.dailyMaintenanceCost = dailyMaintenanceCost;
			this.fund = fund;
			this.stock = stock;
		}

		public double getSetupCost() {
			return setupCost;
		}

		public void setSetupCost(double setupCost) {
			this.setupCost = setupCost;
		}

		public double getDailyMaintenanceCost() {
			return dailyMaintenanceCost;
		}

		public void setDailyMaintenanceCost(double dailyMaintenanceCost) {
			this.dailyMaintenanceCost = dailyMaintenanceCost;
		}

		public double getFund() {
			return fund;
		}

		public void setFund(double fund) {
			this.fund = fund;
		}

		public List<Food> getStock() {
			return stock;
		}

		public void setStock(List<Food> stock) {
			this.stock = stock;
		}
		
	
		
		
}
