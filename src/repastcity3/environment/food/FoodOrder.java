package repastcity3.environment.food;

import java.util.HashMap;
import java.util.List;

import repastcity3.agent.People;
import repastcity3.environment.Building;

public class FoodOrder {
	private HashMap<Food, Double> list;
	private double income;
	private Building source;
	private People agent;

	public FoodOrder(Building source, People agent) {
		this.list = new HashMap<>();
		this.source = source;
		this.agent = agent;
	}

	public FoodOrder() {
		this.list = new HashMap<>();
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

	public double getIncome() {
		return income;
	}

	public void setIncome(double income) {
		this.income = income;
	}

	public Building getSource() {
		return source;
	}

	public void setSource(Building source) {
		this.source = source;
	}

	public People getAgent() {
		return agent;
	}

	public void setAgent(People agent) {
		this.agent = agent;
	}

}
