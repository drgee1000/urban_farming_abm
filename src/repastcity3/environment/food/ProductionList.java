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
	
	public void addFood(Food food)
	{
		FoodEntry foodEntry=new FoodEntry(food);
		list.add(foodEntry);
	}
	
	
	public ArrayList<FoodEntry> getList()
	{
		return list;
	}
	
	
	
}


