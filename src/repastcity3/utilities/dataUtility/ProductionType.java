package repastcity3.utilities.dataUtility;

public class ProductionType {
	private int type;
	private double density; // kg/sqm
	private int period; //in weeks
	private double price;
	public ProductionType(int type, double density, int period, double price) {
		this.type = type;
		this.density = density;
		this.period = period;
		this.price = price;
	}
	public String toString() {
		String s ="type:"+ Integer.toString(this.type)+"density:"+Double.toString(density)+"period:"+Integer.toString(this.period)+"price"+Double.toString(this.price);
		return s;
		
	}
}
