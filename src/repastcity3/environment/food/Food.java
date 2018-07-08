package repastcity3.environment.food;

public class Food {

	private String name;
	private String type;

	private double amount;
	private double calory;
	private double productionCost;
	private double nutrition;
	private double price;
	// unit: tick
	private double productionTime;
	private double expireTime;
	
	public Food(String name, String type, double amount,double price, double calory, double productionCost,
			 double productionTime, double expireTime) {
		super();
		this.name = name;
		this.type = type;
		this.amount = amount;
		this.calory = calory;
		this.productionCost = productionCost;
		this.price = price;
		this.productionTime = productionTime;
		this.expireTime = expireTime;
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
	public double getCalory() {
		return calory;
	}
	public void setCalory(double calory) {
		this.calory = calory;
	}
	public double getProductionCost() {
		return productionCost;
	}
	public void setProductionCost(double productionCost) {
		this.productionCost = productionCost;
	}
	public double getNutrition() {
		return nutrition;
	}
	public void setNutrition(double nutrition) {
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