package repastcity3.environment.food;

import repastcity3.utilities.Helper;

public class FoodEntry {
	Food food;
	private int productionTick;
	boolean isExpired;
	
	public FoodEntry(Food f) {
		this.food = f;
		this.productionTick = Helper.getCurrentTick() + f.getProductionTime();
		this.isExpired = false;
	}

	public int getProductionTick() {
		return productionTick;
	}
	public void check(int tick) {
		if (tick - productionTick > food.getExpireTime())
			isExpired = true;
	}
	public Food getFood(){
		return this.food;
	}
	public boolean expired() {
		if (this.isExpired)
			return true;
		else 
			return false;
	}
}
