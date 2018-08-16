package repastcity3.environment.food;

import com.google.gson.annotations.Expose;

public class Food implements Comparable<Food>{
	@Expose()
	private String name;
	@Expose()
	private String type;//TODO change to enum
	@Expose()
	private double amount;
	@Expose()
	private double productionCost;
	
	private Nutrition nutrition;
	@Expose()
	private double price;
	// unit: tick
	@Expose()
	private double productionTime;
	@Expose()
	private double expireTime;
	
	public Food(String name, String type, double amount,double price, double productionCost,Nutrition nutrition, 
			 double productionTime, double expireTime) {
		super();
		this.name = name;
		this.type = type;
		this.amount = amount;
		this.nutrition=nutrition;
		this.productionCost = productionCost;
		this.price = price;
		this.productionTime = productionTime;
		this.expireTime = expireTime;
	}
	
	public Food() {
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
	public double getProductionTime() {
		return productionTime;
	}
	public void setProductionTime(double productionTime) {
		this.productionTime = productionTime;
	}
	public double getExpireTime() {
		return expireTime;
	}
	public void setExpireTime(double expireTime) {
		this.expireTime = expireTime;
	}
	@Override
	public int compareTo(Food f) {
		if(this.price >= f.getPrice()) {
			return 1;
		}
		return -1;
	}

	
}