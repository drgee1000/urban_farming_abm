package repastcity3.utilities.dataUtility;

public class ProductionType {
	private int type;
	private double density; // kg/sqm
	private int period; // in weeks
	private double price;
	private double tech;
	private double capacity;
	private double priceFactor;

	public ProductionType(int type, double density, int period, double price, double tech, double capacity,
			double priceFactor) {
		this.type = type;
		this.density = density;
		this.period = period;
		this.price = price;
		this.tech = tech;
		this.capacity = capacity;
		this.priceFactor = priceFactor;
	}

	public int getType() {
		return type;
	}

	public double getDensity() {
		return density;
	}

	public int getPeriod() {
		return period;
	}

	public double getPrice() {
		return price;
	}

	public double getTech() {
		return tech;
	}

	public double getCapacity() {
		return capacity;
	}

	public double getPriceFactor() {
		return priceFactor;
	}

	public String toString() {
		String s = "type:" + Integer.toString(this.type) + "density:" + Double.toString(density) + "period:"
				+ Integer.toString(this.period) + "price" + Double.toString(this.price);
		return s;

	}
}
