package repastcity3.utilities.dataUtility;

public class FarmType {
	private int type;
	private double percentage;
	private double density; // kg/sqm
	private int period; // in weeks
	private double price;
	private double tech;
	private double capacity;
	private double priceFactor;
	private double area;

	public FarmType(int type, double percentage, double density, int period, double price, double tech, double capacity,
			double priceFactor,double area) {
		this.type = type;
		this.percentage = percentage;
		this.density = density;
		this.period = period;
		this.price = price;
		this.tech = tech;
		this.capacity = capacity;
		this.priceFactor = priceFactor;
		this.area=area;
	}
	public int getType() {
		return type;
	}
	public double getPercentage() {
		return percentage;
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
		String s = "percentage:" + Double.toString(this.percentage) + "density:" + Double.toString(density) + "period:"
				+ Integer.toString(this.period) + "price" + Double.toString(this.price);
		return s;

	}

	public double getArea() {
		return area;
	}

	public void setArea(double area) {
		this.area = area;
	}
}
