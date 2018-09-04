package repastcity3.environment.food;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import repastcity3.utilities.Helper;


public class ProductionList {
	ArrayList<FoodEntry> list;
	
	public ProductionList() {
		list=new ArrayList<>();
	}
	
	public void addFood(Food food,double amount)
	{
		FoodEntry foodEntry=new FoodEntry(food.getName(),food.getType(), Helper.getCurrentTick(), amount);
	}
	
}

class FoodEntry
{
	String name;
	String type;
	int productionTime;
	double amount;
	public FoodEntry(String name, String type, int productionTime, double amount) {
		this.name = name;
		this.type = type;
		this.productionTime = productionTime;
		this.amount = amount;
	}
	
}
