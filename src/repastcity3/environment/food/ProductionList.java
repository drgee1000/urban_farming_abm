package repastcity3.environment.food;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import repastcity3.utilities.Helper;


public class ProductionList {
	private ArrayList<FoodEntry> list;
	
	public ProductionList() {
		list=new ArrayList<>();
	}
	
	public void addFood(Food food,double amount)
	{
		FoodEntry foodEntry=new FoodEntry(food.getName(),food.getType(), Helper.getCurrentTick(), amount,(int)food.getExpireTime());
	}
	
	public void addFood(Food food,double amount,int tick)
	{
		FoodEntry foodEntry=new FoodEntry(food.getName(),food.getType(), tick, amount,(int)food.getExpireTime());
	}
	
	public ArrayList<FoodEntry> getList()
	{
		return list;
	}
	
	
	public static class FoodEntry
	{
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
			isExpired=(tick-productionTime)>expireTime;
			return isExpired;
		}
		
	}
}


