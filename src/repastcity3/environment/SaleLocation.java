package repastcity3.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import repastcity3.agent.IAgent;
import repastcity3.environment.food.Food;
import repastcity3.environment.food.Waste;

public abstract class SaleLocation extends Building implements IAgent {
	// for farm data
	protected double setupCost;
	protected double dailyMaintenanceCost;
	protected double fund;
	protected HashMap<String, List<Food>> stock;

	protected double scale;
	protected HashMap<String, List<Waste>> waste;

	// public abstract void produce();

	public SaleLocation(double setupCost, double dailyMaintenanceCost, double fund) {
		this.setupCost = setupCost;
		this.dailyMaintenanceCost = dailyMaintenanceCost;
		this.fund = fund;
		this.stock = new HashMap<String, List<Food>>();
		this.scale = 1;

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

	// public HashMap<String,List<FoodEntry>> getStock() {
	// return stock;
	// }

	public void setStock(HashMap<String, List<Food>> stock) {
		this.stock = stock;
	}

	public HashMap<String, List<Waste>> getWaste() {
		// TODO Auto-generated method stub
		return waste;
	}

}