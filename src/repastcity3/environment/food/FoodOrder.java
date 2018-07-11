package repastcity3.environment.food;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.analysis.function.Add;

public class FoodOrder {
	private HashMap<Food, Double> list;




	public FoodOrder() {
		list = new HashMap<>();
	}

	public void addOrder(Food food, double amount) {
		if (!list.containsKey(food)) {
			list.put(food, amount);
		} else {
			list.put(food, list.get(food) + amount);
		}
	}
	
	public HashMap<Food, Double> getList() {
		return list;
	}

}
