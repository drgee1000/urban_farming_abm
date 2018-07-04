package repastcity3.environment;

public class Food {

	private String name;
	// todo: use Enum?
	private String type;

	private int amount;
	private double calory;
	private double productionCost;
	//private double nutrition;

	// unit: tick
	private double productionTime;
	private double expireTime;

	public Food(String name, String type, int amount, double productionCost, double calory, double productionTime,
			double expireTime) {
		super();
		this.name = name;
		this.type = type;
		this.amount = amount;
		this.productionCost = productionCost;
		this.calory = calory;
		this.productionTime = productionTime;
		this.expireTime = expireTime;
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

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public double getProductionCost() {
		return productionCost;
	}

	public void setProductionCost(double productionCost) {
		this.productionCost = productionCost;
	}

	public double getCalory() {
		return calory;
	}

	public void setCalory(double nutrition) {
		this.calory = nutrition;
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
