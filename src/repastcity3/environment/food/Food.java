package repastcity3.environment.food;

public class Food {

	private String name;
	private String type;//TODO change to enum

	private double amount;
//	private double calory;
	private double productionCost;
	private Nutrition nutrition;
	private double price;
	// unit: tick
	private double productionTime;
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
	

	
}