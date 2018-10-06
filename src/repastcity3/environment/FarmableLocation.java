package repastcity3.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import repastcity3.agent.IAgent;
import repastcity3.environment.food.Food;
import repastcity3.environment.food.FoodEntry;
import repastcity3.environment.food.ProductionList;



public abstract class FarmableLocation extends Building implements IAgent {
	// for farm data
	protected double setupCost;
	protected double dailyMaintenanceCost;
	protected double fund;
	protected HashMap<String,List<Food>> stock;
	protected ProductionList productionList;
	protected double scale;

	//public  abstract void produce();

	public FarmableLocation(double setupCost, double dailyMaintenanceCost, double fund,
							HashMap<String,List<Food>> stock) {
		this.setupCost = setupCost;
		this.dailyMaintenanceCost = dailyMaintenanceCost;
		this.fund = fund;
		this.stock = stock;
		this.scale=1;
		this.productionList=new ProductionList();
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

	//public HashMap<String,List<FoodEntry>> getStock() {
	//	return stock;
	//}

	public void setStock(HashMap<String,List<Food>> stock) {
		this.stock = stock;
	}




}