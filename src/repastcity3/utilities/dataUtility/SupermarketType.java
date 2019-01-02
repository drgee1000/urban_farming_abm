package repastcity3.utilities.dataUtility;

public class SupermarketType {
	private double ratio;
	private int urbanPeriod;
	private int exPeriod; //in weeks
	private double sourcingPlan;
	private double stockThreshold;
	private double radius;
	private double priceFactor;
	public SupermarketType(double ratio, int uP, int eP, double sP, double sT, double r, double pf) {
		this.ratio = ratio;
		this.urbanPeriod = uP;
		this.exPeriod = eP;
		this.sourcingPlan = sP;
		this.stockThreshold = sT;
		this.radius = r;
		this.priceFactor = pf;
		
	}
	public double getRatio() {
		return ratio;
	}
	public int getUrbanPeriod() {
		return urbanPeriod;
	}
	public int getExPeriod() {
		return this.exPeriod;
	}
	public double getSourcingPlan() {
		return this.sourcingPlan;
	}
	public double getStockThreshold() {
		return this.stockThreshold;
	}
	public double getRadius() {
		return this.radius;
	}
	public double getPriceFactor() {
		return this.priceFactor;
	}
	
}
