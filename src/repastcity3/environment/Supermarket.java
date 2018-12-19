package repastcity3.environment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.RandomAccess;
import java.util.Set;

import cern.jet.random.Uniform;
import repast.simphony.random.RandomHelper;
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
	private int urbanSourcePeriod;
	private int externalSourcePeriod;
	// private PriorityQueue<FoodEntry> productionQueue;

	private HashMap<String, Double> stockCalorieCount;
	//private HashMap<String, Double> stockThreshold;
	private double stockThreshold;
	private HashMap<String, Double> sourcingPlan;
	private Set<String> types;
	
	private static int uniqueID = 0;
	private int id;
	private static double urbanPriceFactor = 1.1; //price = originalPrice * priceFactor

	public Supermarket() {
		// setup Cost, daily maintainance cost, fund
		super(1000, 100, 50000);
		this.id = uniqueID++;
		
		urbanSourcePeriod = 7;
		externalSourcePeriod = getExternalSourcePeriod();
		
		types = new HashSet<String>();
		types.add("vegetable");
		waste = new HashMap<String, List<FoodEntry>>();
		stockCalorieCount = new HashMap<String, Double>();
		sourcingPlan = new HashMap<String, Double>();
		this.agents = new ArrayList<IAgent>();
		
		this.stockThreshold = 200000;
		initStock();
		

	}
	private int getExternalSourcePeriod() {
		Uniform nRand = RandomHelper.getUniform();
		if(nRand.nextBoolean()) {
			return nRand.nextIntFromTo(4, 7);
		}else {
			return nRand.nextIntFromTo(1, 3);
		}
	}

	public HashMap<String, List<FoodEntry>> getWaste() {
		return this.waste;
	}

	private void initStock() {
		// initially, food are from external sources
		//System.out.println("init stock");
		sourcingPlan.put("vegetable", stockThreshold);
		sourceFromExternalFarm();
		//System.out.println("init stock finish, stock level: " + stockCalorieCount.get("vegetable"));
		// printStock();
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
			food.setPrice(food.getPrice()*1.1);
			if (stock.containsKey(type)) {
				list = stock.get(type);
				
			} else {
				list = new ArrayList<Food>();
				
			}
			list.add(food);
			stock.put(type, list);
			countStockCalorie(food);
			// //System.out.println("add stock amount: " + food.getAmount());
		}
	}

	private void addWaste(FoodEntry fe) {
		// Food food = fe.getFood();
		//System.out.println(this.toString()+"add waste "+fe.getName()+ " @ " + fe.getProductionTick());
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
		//System.out.println(this.toString()+"refresh source plan");
		// Set<String> types = stockCount.keySet();
		for (String type : types) {
			sourcingPlan.put(type,0.0);
			double base = stockCalorieCount.get(type);
			double target = stockThreshold;
			//System.out.println("type:"+type+"base:"+base+" target"+target);
			if (base < stockThreshold) {
				sourcingPlan.put(type, 1.2*(target-base));
			}

		}
		//System.out.println(this.toString()+" plan:" + sourcingPlan.get("vegetable"));

	}

	private boolean planEmpty() {
		for (double d : sourcingPlan.values()) {
			if (d != 0.0)
				return false;
		}
		return true;
	}

	private void printSourcingPlan() {
		//System.out.println("=======================");
		//System.out.println(this.toString());
		for (String type : sourcingPlan.keySet()) {
			//System.out.println(type + "  " + sourcingPlan.get(type));
		}
	}
	private void sourceFromExternalFarm() {
		int tick=Helper.getCurrentTick();
		////System.out.println(this.toString()+"source from external farm");
		List<Food> foodList = FoodUtility.getSupermarketFoodList();
		Random r = new Random();
		double target = sourcingPlan.get("vegetable"); 
		Food f = new Food();
		int size = foodList.size();
		for (int i = 0; i < size-1;i++) {
			f = foodList.get(i);
			int x = r.nextInt((int)target);
			target -= x;
			f.setAmount(x/f.getDensity());
			f.setSource("external");
			f.setProductionTick(tick);
			addStock(f);
		}
		f = foodList.get(size-1);
		f.setAmount(target/f.getDensity());
		f.setSource("external");
		f.setProductionTick(tick);
		addStock(f);
	}
	private void sourceFromUrbanFarm() {
		Food food;
		/*
		 * purchase food according to plan the loop ends when need is satisfied or
		 * there's no other farm to go
		 */

		Iterator<Farm> iter = new RandomIterator<Farm>(ContextManager.farmContext.iterator());
		while (iter.hasNext()) { // loop through all farms
			FoodOrder fo = new FoodOrder();
			Farm f = iter.next();
			HashMap<String, List<Food>> fStock = f.getStock();
			for (String type : sourcingPlan.keySet()) { // loop through all kinds of stock of a farm
				double target = sourcingPlan.get(type);
				// //System.out.println();
				// System.out.println("type: " + type + " target: " + target);
				List<Food> foodOfType = fStock.get(type);
				if(foodOfType != null) {
					int len = foodOfType.size();
					for (int i = 0; i < len; i++) {
						if (target > 0.0) {
							// //System.out.println("food of type len:" + foodOfType.size() + "i: " + i);
							Food fd = (Food) foodOfType.get(i);
							double fdCalorie = fd.getDensity()*fd.getAmount();
							food = new Food(fd);
							if (fdCalorie > target) { // buy part of that food
								double amount = (double)(Math.round((target/fd.getDensity())*100)/100.0);
								food.setAmount(target/fd.getDensity());
								fo.addOrder(fd, amount);
							} else { //buy all of that food
								//food = fd;
								fo.addOrder(fd, fd.getAmount());
							}
							food.setSource(f.toString());
							food.setPrice(food.getPrice()*urbanPriceFactor);
							addStock(food);
							// //System.out.println(this.toString()+" purchase " + food.getName() + " amount:
							// " + food.getAmount());
							target = target - food.getDensity()*food.getAmount();
							if (target < 0.0)
								target = 0.0;
							// //System.out.println("target after purchase " + target);
							//update targets in sourceing plan 
							sourcingPlan.put(type, target);
						} else {
							break;
						}
					}
				}

			}
			// //System.out.println("vague purchase fo size: "+fo.getList().size() );
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
//		//System.out.println(this.toString()+"check stock");
		int tick = Helper.getCurrentTick();
		for (String name : stock.keySet()) {
			List<Food> foods = stock.get(name);
			for (int i = 0; i < foods.size(); i++) {
				Food food = foods.get(i);
				food.check(tick);
				if (food.expired()) {
					foods.remove(food);
					//System.out.println("------------"+this.toString()+" expires "+food.getDensity()*food.getAmount());
					//double n1 = stockCalorieCount.get(food.getType());
					double n2 = stockCalorieCount.get(food.getType())-food.getDensity()*food.getAmount();
					stockCalorieCount.put(food.getType(),n2);
					//System.out.println("------------"+this.toString()+ "  "+n1+"->"+n2);
					addWaste(new FoodEntry(food));
				}
			}

			// stock.put(name, foods);
		}

	}

	@Override
	public void step() {
		//System.out.println("supermarket "+this.id+" start");
//		//System.out.println("Supermarket" + this.toString() + "step");
		tick = Helper.getCurrentTick();
		checkStock();
		// source from urban farm
		if(tick%this.externalSourcePeriod == 1) {
			refreshSourcingPlan();
			if(!planEmpty()) {
				sourceFromExternalFarm();
			}
		}
		if(tick% this.urbanSourcePeriod == 1)
		{
			refreshSourcingPlan();
			if (!planEmpty()) {
				sourceFromUrbanFarm();
			}

		}
		//printStock();
		//System.out.println("supermarket "+this.id+" end");

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
		if(order.getList().size() == 0)
			return;
		//System.out.println("====================="+this.toString()+"sell");
		
		HashMap<Food, Double> list = order.getList();
		double income = this.fund;
		list.forEach((food, amount) -> {
			String type = food.getType();
			//System.out.println(food.getName()+": "+food.getAmount()+" ordered: "+amount);
			double newAmount = food.getAmount() - amount;
			if (newAmount > 0) { // food bought partially
				food.setAmount(newAmount);
			} else { // food bought
				List<Food> stockList = stock.get(food.getType());
				//System.out.println(this.toString()+"==========remove stock");
				stockList.remove(food);
			}
			double stockCountNum = stockCalorieCount.get(type);
			
			
			stockCalorieCount.put(type, stockCountNum - food.getDensity()*amount);
			//System.out.println(this.toString()+"sell,  stock:"+stockCountNum +"->"+(stockCountNum - food.getDensity()*amount));
			this.fund += amount * food.getPrice();
		});
		income = this.fund - income;
		//record sales
		synchronized(ContextManager.dLogger) {
			try {
				ContextManager.dLogger.recordSale(order, Helper.getCurrentTick(), income, this.toString(),consumerID);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

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
		//double calory = 0;
		//for (double clry:stockCalorieCount.values()) {
//			calory += clry;
		//}
		return stockCalorieCount.get("vegetable");
	}

	private void printStock() {
		System.out.println(this.toString() + "  print stock");
		for (String type : stock.keySet()) {
			System.out.println(type);
			int len = stock.get(type).size();
			for (int i = 0; i < len; i++) {
				Food f = stock.get(type).get(i);
				System.out.println(this.toString() + "  " + f.getName() + "  " + (int)f.getAmount() + " @" + f.getProductionTick()+" from "+f.getSource());
			}
		}

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