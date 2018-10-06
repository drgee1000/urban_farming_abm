package repastcity3.environment.food;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import repastcity3.utilities.Helper;


public class ProductionList {
	private ArrayList<Food> list;
	
	public ProductionList() {
		list=new ArrayList<>();
	}
	
	public void addFood(Food food)
	{
		list.add(food);
	}
	
	
	public ArrayList<Food> getList()
	{
		return list;
	}
	
	
	
}


