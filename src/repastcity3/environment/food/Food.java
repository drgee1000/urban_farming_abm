package repastcity3.environment.food;

import com.google.gson.annotations.Expose;

import repastcity3.utilities.Helper;

public class Food implements Comparable<Food>{
	@Expose()
	private String name;
	
	//@Expose()
	private String type;
	@Expose()
	private double amount;
	private double productionCost;
	
	private Nutrition nutrition;
	
	private double price;
	// unit: tick
	
	private double calorie;
	
	public int productionTime;
	
	private int expireTime;
	private String source;
	@Expose
	public int productionTick;
	boolean isExpired;
	public Food(String name, String type, double calorie,double amount,double price, double productionCost,Nutrition nutrition,
			 int productionTime, int expireTime) {
		super();
		this.name = name;
		this.type = type;
		this.calorie=calorie;
		this.amount = amount;
		this.nutrition=nutrition;
		this.productionCost = productionCost;
		this.price = price;
		this.productionTime = productionTime;
		this.expireTime = expireTime;
	}
	
	public Food() {
	}
	
	public Food(Food fd) {
		// TODO Auto-generated constructor stub
		this.name = fd.getName();
		this.type = fd.getType();
		this.calorie = fd.getCalorie();
		this.amount = fd.getAmount();
		this.nutrition = fd.getNutrition();
		this.productionCost = fd.getProductionCost();
		this.productionTick = fd.getProductionTick();
		this.expireTime = fd.expireTime;
		this.source = fd.getSource();
		this.price = fd.getPrice();
	}

	public String getSource() {
		return source;
	}
	public void setSource(String s) {
		source = s;
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
		if(this.price >= f.getPrice()) {
			return 1;
		}
		return -1;
	}
	

	public int getProductionTick() {
		return productionTick;
	}
	public void setProductionTick(int t) {
		this.productionTick = t;
	}
	public void check(int tick) {
		if (tick - productionTick > expireTime)
			isExpired = true;
	}
	public boolean expired() {
		if (this.isExpired)
			return true;
		else 
			return false;
	}
	
}