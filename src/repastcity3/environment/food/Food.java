package repastcity3.environment.food;

import com.google.gson.annotations.Expose;

import repastcity3.utilities.Helper;

public class Food implements Comparable<Food> {

	private String name;
	private String type;
	private double amount;
	private double productionCost;
	private Nutrition nutrition;
	private double price;
	private double calorie;
	private int productionTime;
	private int expireTime;
	private int productionTick;
	boolean isExpired;

	public Food(String name, String type, double calorie, double amount, double price, double productionCost,
			Nutrition nutrition, int productionTime, int expireTime) {
		super();
		this.name = name;
		this.type = type;
		this.calorie = calorie;
		this.amount = amount;
		this.nutrition = nutrition;
		this.productionCost = productionCost;
		this.price = price;
		this.productionTime = productionTime;
		this.expireTime = expireTime;
		this.productionTick = Helper.getCurrentTick();
		this.isExpired = false;
	}

	public Food() {
	}
	
	public int getFreshness() {
		int freshness=this.productionTick+this.expireTime-Helper.getCurrentTick();
//		if(freshness<0) {
//			isExpired = true;
//		}
		return freshness;
	}

	public void checkExpired(int tick) {
		if (tick - productionTick > this.getExpireTime())
			isExpired = true;
	}
	
	public int getProductionTick()  {
		return this.productionTick;
	}
	
	public void checkExpired() {
		if (Helper.getCurrentTick() - productionTick > this.getExpireTime())
			isExpired = true;
	}

	public boolean isExpired() {
		return this.isExpired;
	}

	public double getCalorie() {
		return calorie;
	}

	public void setCalorie(double calorie) {
		this.calorie = calorie;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public double getProductionCost() {
		return productionCost;
	}

	public void setProductionCost(double productionCost) {
		this.productionCost = productionCost;
	}

	public Nutrition getNutrition() {
		return nutrition;
	}

	public void setNutrition(Nutrition nutrition) {
		this.nutrition = nutrition;
	}

	public int getProductionTime() {
		return productionTime;
	}

	public void setProductionTime(int productionTime) {
		this.productionTime = productionTime;
	}

	public double getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(int expireTime) {
		this.expireTime = expireTime;
	}

	@Override
	public int compareTo(Food f) {
		if (this.price >= f.getPrice()) {
			return 1;
		}
		return -1;
	}

}