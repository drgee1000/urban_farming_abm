package repastcity3.environment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import repastcity3.agent.Farm;
import repastcity3.agent.IAgent;
import repastcity3.environment.food.FoodUtility;
import repastcity3.environment.food.Food;
import repastcity3.environment.food.FoodOrder;
import repastcity3.environment.food.FoodEntry;
import repastcity3.main.ContextManager;
import repastcity3.utilities.Helper;

public class Supermarket extends SaleLocation implements FixedGeography {

	private int tick;
	private double score;
	private int score_count;
	private List<Food> purchasePlan;
	// private PriorityQueue<FoodEntry> productionQueue;

	private HashMap<String, Double> stockCalorieCount;
	private HashMap<String, Double> stockThreshold;
	private HashMap<String, Double> sourcingPlan;
	private Set<String> types;
	
	private static int uniqueID = 0;
	private int id;

	public Supermarket() {
		// double setupCost,double dailyMaintenanceCost, double fund,List<Food> stock
		super(1000, 100, 50000);
		this.id = uniqueID++;
		types = new HashSet<String>();
		waste = new HashMap<String, List<FoodEntry>>();
		stockCalorieCount = new HashMap<String, Double>();
		sourcingPlan = new HashMap<String, Double>();
		this.agents = new ArrayList<IAgent>();
		// this.count = 0;
		this.purchasePlan = FoodUtility.getRandomFoodList(1000, 3000);
		// for (Food f:purchasePlan) {
		// System.out.println("plan, amount: " + f.getAmount());
		// }
		initStock();
		// System.out.println("init supermarket "+identifier+" with stockCount " +
		// stock.keySet().size());
		// List<Food> l = this.stock.get("grain");
		// for(Food f:l) {
		// System.out.println(f.getAmount());
		// }

	}

	public HashMap<String, List<FoodEntry>> getWaste() {
		return this.waste;
	}

	private void initStock() {
		// set threshould as initial stock level and add food in purchasePlan to stock
		stockThreshold = new HashMap<String, Double>();
		for (Food food : purchasePlan) {
			food.setProductionTick(0);

			String type = food.getType();
			types.add(type);
			// init threshold
			if (!stockThreshold.containsKey(type)) {
				stockThreshold.put(type, food.getAmount()*food.getDensity());
			} else {
				double x = stockThreshold.get(type) + food.getAmount()*food.getDensity();
				stockThreshold.put(type, x);
			}
			food.setAmount(0);
			countStockCalorie(food);
			// add to stock
			// addStock(food);
		}

		//HashMap<String, List<Food>> astock = getStock();
		//for (String t : astock.keySet()) {
			// System.out.println("supermarket"+identifier+"has food in "+ t+":
			// "+astock.get(t).size());
		//}
	}

	private void countStockCalorie(Food food) {
		String type = food.getType();
		if (!stockCalorieCount.containsKey(type)) {
			stockCalorieCount.put(type, food.getDensity()*food.getAmount());
		} else {
			double x = stockCalorieCount.get(type) + food.getDensity()*food.getAmount();
			stockCalorieCount.put(type, x);
		}
	}

	private void addStock(Food f) {
		Food food = new Food(f);
		String type = food.getType();
		List<Food> list;
		if (f.getAmount() > 0) {
			if (stock.containsKey(type)) {
				list = stock.get(type);
				list.add(food);
			} else {
				list = new ArrayList<Food>();
				list.add(food);
				stock.put(type, list);
			}
			// System.out.println("add stock amount: " + food.getAmount());
		}
	}

	private void addWaste(FoodEntry fe) {
		// Food food = fe.getFood();
		String type = fe.getType();
		List<FoodEntry> list;
		if (waste.containsKey(type)) {
			list = waste.get(type);
			list.add(fe);
		} else {
			list = new ArrayList<FoodEntry>();
			list.add(fe);
			waste.put(type, list);
		}
	}

	private void refreshSourcingPlan() {
		// Set<String> types = stockCount.keySet();
		for (String type : types) {
			double base = stockCalorieCount.get(type);
			double target = stockThreshold.get(type);
			if (stockCalorieCount.get(type) < stockThreshold.get(type)) {
				sourcingPlan.put(type, 2*(target-base));
			}

		}

	}

	private boolean planEmpty() {
		for (double d : sourcingPlan.values()) {
			if (d != 0.0)
				return false;
		}
		return true;
	}

	private void printVPlan() {
		System.out.println("=======================");
		System.out.println(this.toString());
		for (String type : sourcingPlan.keySet()) {
			System.out.println(type + "  " + sourcingPlan.get(type));
		}
	}

	private void sourceFromUrbanFarm() {
		Food food;
		/*
		 * purchase food according to plan the loop ends when need is satisfied or
		 * there's no other farm to go
		 */
		
		// printVPlan();

		Iterator<Farm> iter = new RandomIterator<Farm>(ContextManager.farmContext.iterator());
		while (iter.hasNext()) { // loop through all farms
			FoodOrder fo = new FoodOrder();
			Farm f = iter.next();
			HashMap<String, List<Food>> fStock = f.getStock();
			for (String type : sourcingPlan.keySet()) { // loop through all kinds of stock of a farm
				double target = sourcingPlan.get(type);
				// System.out.println();
				// System.out.println("type: " + type + " target: " + target);
				List<Food> foodOfType = fStock.get(type);
				int len = foodOfType.size();
				// System.out.println("food of type len:" + len);
				if (len > 0) {
					for (int i = 0; i < len; i++) {
						if (target > 0.0) {
							// System.out.println("food of type len:" + foodOfType.size() + "i: " + i);
							Food fd = (Food) foodOfType.get(i);
							double fdCalorie = fd.getDensity()*fd.getAmount();
							food = new Food(fd);
							if (fdCalorie > target) { // buy part of that food
								double amount = (double)(Math.round((target/fd.getDensity())*100)/100.0);
								food.setAmount(target/fd.getDensity());
								fo.addOrder(fd, amount);
							} else { //buy all of that food
								food = fd;
								fo.addOrder(food, food.getAmount());
							}
							food.setSource(f.toString());
							addStock(food);
							countStockCalorie(food);
							// System.out.println(this.toString()+" purchase " + food.getName() + " amount:
							// " + food.getAmount());
							target = target - food.getDensity()*food.getAmount();
							if (target < 0.0)
								target = 0.0;
							// System.out.println("target after purchase " + target);
							sourcingPlan.put(type, target);
						} else {
							break;
						}
					}
				}

			}
			// System.out.println("vague purchase fo size: "+fo.getList().size() );
			f.sell(fo, this.toString());
			// stop the loop if requirement is met
			if (planEmpty()) {
				break;
			}
		}
	}

	public HashMap<String, List<Food>> getStock() {

		return stock;
	}

	public void checkStock() {
//		System.out.println(this.toString()+"check stock");
		int tick = Helper.getCurrentTick();
		for (String name : stock.keySet()) {
			List<Food> foods = stock.get(name);
			for (int i = 0; i < foods.size(); i++) {
				Food food = foods.get(i);
				food.check(tick);
				if (food.expired()) {
					foods.remove(food);
					addWaste(new FoodEntry(food));
				}
			}

			// stock.put(name, foods);
		}

	}

	@Override
	public void step() {
		System.out.println("supermarket "+this.id+" start");
//		System.out.println("Supermarket" + this.toString() + "step");
		tick = Helper.getCurrentTick();
		checkStock();
		// if(tick%144==30)
		{
			// refreshPurchasePlan();
			refreshSourcingPlan();
			if (!planEmpty()) {
				sourceFromUrbanFarm();
			}

		}
		System.out.println("supermarket "+this.id+" end");

	}

	public void updateScore(double newScore) {
		this.score_count += 1;
		double score_sum = this.score + newScore;
		this.score = score_sum / score_count;
	}

	public double getScore() {
		return this.score;
	}

	@Override
	public boolean isThreadable() {
		return true;
	}

	public boolean isAvailable() {
		return stock.keySet().size() > 0;
	}

	public void sell(FoodOrder order, String consumerID) {
		HashMap<Food, Double> list = order.getList();
		double income = this.fund;
		list.forEach((food, amount) -> {
			String type = food.getType();
			double newAmount = food.getAmount() - amount;
			if (newAmount > 0) { // food bought partially
				food.setAmount(newAmount);
			} else { // food bought
				List<Food> stockList = stock.get(food.getType());
				// System.out.println("remove stock");
				stockList.remove(food);
			}
			double stockCountNum = stockCalorieCount.get(type);
			stockCalorieCount.put(type, stockCountNum - food.getDensity()*food.getAmount());
			this.fund += amount * food.getPrice();
		});
		income = this.fund - income;
		//record sales
//		synchronized(ContextManager.dLogger) {
//			try {
//				ContextManager.dLogger.recordSale(order, Helper.getCurrentTick(), totalIncome, this.toString(),consumerID);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}

		// let order be collected by GC
		order = null;
	}

	public class feComparator implements Comparator<FoodEntry> {
		//compare food according to productionTick
		public int compare(FoodEntry fe1, FoodEntry fe2) {

			if (fe1.getProductionTick() < fe2.getProductionTick())
				return -1;
			else if (fe1.getProductionTick() == fe2.getProductionTick())
				return 0;
			else
				return 1;
		}
	}

	@Override
	public String toString() {
		return "Supermarket: " + this.identifier;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Supermarket))
			return false;
		Supermarket b = (Supermarket) obj;
		return this.identifier.equals(b.identifier);
	}
	public double getStockCalorie() {
		double calory = 0;
		for (double clry:stockCalorieCount.values()) {
			calory += clry;
		}
		return calory;
	}

}

//http://www.java2s.com/Tutorials/Java/Collection_How_to/Iterator/Create_random_Iterator.htm
class RandomIterator<T> implements Iterator<T> {
	private final Iterator<T> iterator;

	public RandomIterator(final Iterator<T> i) {
		final List<T> items;

		items = new ArrayList<T>();

		while (i.hasNext()) {
			final T item;

			item = i.next();
			items.add(item);
		}

		Collections.shuffle(items);
		iterator = items.iterator();
	}

	public boolean hasNext() {
		return (iterator.hasNext());
	}
	
	public T next() {
		return (iterator.next());
	}

	public void remove() {
		iterator.remove();
	}
}