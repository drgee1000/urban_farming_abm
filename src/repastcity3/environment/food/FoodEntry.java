package repastcity3.environment.food;

public class FoodEntry {
	String name;
	String type;
	int productionTime;
	double amount;
	int expireTime;
	boolean isExpired;
	
	public FoodEntry(String name, String type, int productionTime, double amount, int expireTime) {
		super();
		this.name = name;
		this.type = type;
		this.productionTime = productionTime;
		this.amount = amount;
		this.expireTime = expireTime;
		this.isExpired=false;
	}

	public int getProductionTime() {
		return productionTime;
	}
	
	public boolean checkExpired(int tick)
	{
		isExpired=tick-productionTime>expireTime;
		return isExpired;
	}
}
