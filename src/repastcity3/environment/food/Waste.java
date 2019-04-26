package repastcity3.environment.food;

import com.google.gson.annotations.Expose;

import repastcity3.utilities.Helper;

public class Waste {
	//@Expose()
	//Food food;
	@Expose()
	private int productionTick;
	@Expose()
	String name;
	@Expose()
	Double amount;
	String type;
	
	public Waste(Food f) {
		//this.food = f;
		name = f.getName();
		type = f.getType();
		amount = f.getAmount();
		productionTick = f.getProductionTick();
		
	}

	public int getProductionTick() {
		return productionTick;
	}
	public String getName() {
		return name;
	}
	public String getType() {
		return type;
	}
	
	
}
