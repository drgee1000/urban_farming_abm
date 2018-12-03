package repastcity3.environment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import repast.simphony.util.collections.IndexedIterable;
import repastcity3.agent.Farm;
import repastcity3.agent.IAgent;
import repastcity3.environment.food.DefaultFoodStock;
import repastcity3.environment.food.Food;
import repastcity3.environment.food.FoodOrder;
import repastcity3.environment.food.FoodEntry;
import repastcity3.main.ContextManager;
import repastcity3.utilities.Helper;

public class Supermarket extends SaleLocation implements FixedGeography{

	private int tick;
	private double score;
	private int score_count;
	private List<Food> purchasePlan;
	//private PriorityQueue<FoodEntry> productionQueue;
	
	private HashMap<String,Double> stockCount;
	private HashMap<String,Double> stockThreshold;
	private HashMap<String,Double> vaguePurchasePlan;
	private Set<String> types;
	public Supermarket() {
		// double setupCost,double dailyMaintenanceCost, double fund,List<Food> stock
		super(1000, 100, 50000);
		types = new HashSet<String>();
		waste = new HashMap<String,List<FoodEntry>>();
		stockCount = new HashMap<String,Double>();
		vaguePurchasePlan = new HashMap<String,Double>();
		this.agents = new ArrayList<IAgent>();
		//this.count = 0;
		this.purchasePlan = DefaultFoodStock.getRandomFoodList(1000,3000);
		//for (Food f:purchasePlan) {
		//	System.out.println("plan, amount: " + f.getAmount());
		//}
		initStock();
		// System.out.println("init supermarket "+identifier+" with stockCount " + stock.keySet().size());
		// List<Food> l = this.stock.get("grain");
		// for(Food f:l) {
			// System.out.println(f.getAmount());
		// }
		
	}
	public HashMap<String,List<FoodEntry>> getWaste(){
		return this.waste;
	}
	
	private void initStock() {
		stockThreshold = new HashMap<String,Double>();
		for (Food food : purchasePlan) {
			food.setProductionTick(0);
			
			String type = food.getType();
			types.add(type);
			//init threshold
			if(!stockThreshold.containsKey(type)){
				stockThreshold.put(type,food.getAmount());
			} else {
				double x = stockThreshold.get(type) + food.getAmount();
				stockThreshold.put(type, x);
			}
			food.setAmount(0);
			stockCount(food);
			// add to stock
			//addStock(food);
		}
		
		refreshVPurchasePlan();
//		vaguePurchase();
		HashMap<String,List<Food>> astock = getStock();
		for (String t : astock.keySet()) {
			// System.out.println("supermarket"+identifier+"has food in "+ t+":  "+astock.get(t).size());
		}
	}
	private void stockCount(Food food) {
		String type = food.getType();
		if(!stockCount.containsKey(type)){
			stockCount.put(type,food.getAmount());
		} else {
			double x = stockCount.get(type) +  food.getAmount();
			stockCount.put(type, x);
		}
	}
	private void addStock(Food f) {
		Food food = new Food(f);
		String type = food.getType();
		List<Food> list;
		if(f.getAmount()>0) {
		if (stock.containsKey(type)) {
			list = stock.get(type);
			list.add(food);
		} else {
			list = new ArrayList<Food>();
			list.add(food);
			stock.put(type, list);
		}
		//System.out.println("add stock amount: " + food.getAmount());
		}
	}
	private void addWaste(FoodEntry fe) {
		//Food food = fe.getFood();
		String type= fe.getType();
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
	//not used...
	private void refreshPurchasePlan() {
		// make new production plan and insert them to productionQueue
		DefaultFoodStock.getRandomFoodList(1000,3000);
		Set<String> types = stock.keySet();
		List<String> typeList = new ArrayList<String>();
		List<Food> foodList = new ArrayList<Food>();
		for (String type : types) {
			if(stockCount.get(type) < stockThreshold.get(type)) {
				int totalAmount = 0;
				typeList.add(type);
				while (totalAmount < stockThreshold.get(type)) {
					Food f = DefaultFoodStock.getFoodByType(type);
					totalAmount += f.getAmount();
					foodList.add(f);
				}
			}
		}
		purchasePlan = foodList;
		return;
	}
	private void refreshVPurchasePlan() {
		//Set<String> types = stockCount.keySet();
		for(String type:types) {
			double base = stockCount.get(type);
			double target = stockThreshold.get(type);
			if(stockCount.get(type)<stockThreshold.get(type)) {
				double amount = 2*(target-base);
				vaguePurchasePlan.put(type,amount);
			}
			
		}
		
	}
	private boolean planEmpty() {
		for (double d:vaguePurchasePlan.values()) {
			if (d != 0.0) 
				return false;
		}
		return true;
	}
	private void printVPlan() {
		System.out.println("=======================");
		System.out.println(this.toString());
		for (String type:vaguePurchasePlan.keySet()) {
			System.out.println(type+"  " + vaguePurchasePlan.get(type));
		}
	}
	private  void vaguePurchase() {
		Food food;
		/* purchase food according to plan
		 * the loop ends when need is satisfied or there's no other farm to go
		 */
		//printVPlan();
		
		Iterator<Farm> iter = new RandomIterator<Farm>(ContextManager.farmContext.iterator());
		while(iter.hasNext()) { // loop through all farms
			FoodOrder fo = new FoodOrder();
			Farm f = iter.next();
			synchronized(f) {
				HashMap<String,List<Food>> fStock = f.getStock();
				for(String type:vaguePurchasePlan.keySet()) { //loop through all kinds of stock of a farm
					double target = vaguePurchasePlan.get(type);
					//System.out.println();
					//System.out.println("type:  " + type + "  target: " + target);
					List<Food> foodOfType= fStock.get(type);
					int len = foodOfType.size();
					//System.out.println("food of type len:" + len);
					if(len > 0) {
						for (int i = 0; i < len; i++) {
							if (target > 0.0) {
								//System.out.println("food of type len:" + foodOfType.size() + "i: " + i);
								Food fd = (Food) foodOfType.get(i);
								double fdAmount = fd.getAmount();
								food = new Food(fd);
								if(fdAmount > target) {
									food.setAmount(target);
									fo.addOrder(fd,target);
									//fd.setAmount(fdAmount-target);
								} else {
									food = fd;
									fo.addOrder(food,food.getAmount());
								}	
								food.setSource(f.toString());
								addStock(food);							
								stockCount(food);
								//System.out.println(this.toString()+" purchase " + food.getName() + "  amount: " + food.getAmount());
								target = target - food.getAmount();
								if (target < 0.0)
									target = 0.0;
								//System.out.println("target after purchase " + target);
								vaguePurchasePlan.put(type, target);
							} else {
								break;
							}
						}
					}
				
				}
			}
			//System.out.println("vague purchase fo size: "+fo.getList().size() );
			f.sell(fo,this.toString());
			 //stop the loop if requirement is met 
			if(planEmpty()) {
				break;
			}
		}
	}
	public HashMap<String,List<Food>> getStock(){
		
		return stock;
	}

	public void checkStock() {
//		System.out.println(this.toString()+"check stock");
		int tick=Helper.getCurrentTick();
		synchronized (this) {
			for (String name : stock.keySet()) {
				List<Food> foods = stock.get(name);
				for (int i = 0; i < foods.size(); i++) {
					Food food=foods.get(i);
					food.check(tick);
					if (food.expired()) {
						foods.remove(food);
						addWaste(new FoodEntry(food));
					}
				}
			
				//stock.put(name, foods);
			}
		}

	}
	@Override
	public void step() {
//		System.out.println("Supermarket" + this.toString() + "step");
		tick=Helper.getCurrentTick();
		checkStock();
		//if(tick%144==30)
		{
			//refreshPurchasePlan();
			refreshVPurchasePlan();
			if(!planEmpty()) {
				vaguePurchase();
			}
			
		}


	}

	public void updateScore(double newScore) {
		this.score_count += 1;
		double score_sum = this.score + newScore;
		this.score = score_sum/score_count;
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

	public synchronized void sell(FoodOrder order,String consumerID) {
		HashMap<Food, Double> list = order.getList();
		double totalIncome = this.fund;
		list.forEach((food, amount) -> {
			String type = food.getType();
			double newAmount = food.getAmount()-amount;
			if(amount > 0) {
				food.setAmount(food.getAmount() - amount);
			} else {
				List<Food> stockList = stock.get(food.getType());
				//System.out.println("remove stock");
				stockList.remove(food);
			}
			double stockCountNum = stockCount.get(type);
			stockCount.put(type, stockCountNum - newAmount);
			this.fund += amount * food.getPrice();
			//count -= amount;
		});
		totalIncome = this.fund - totalIncome;
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
		public int compare(FoodEntry fe1, FoodEntry fe2) {

			if(fe1.getProductionTick() < fe2.getProductionTick())
				return -1;
			else if(fe1.getProductionTick() == fe2.getProductionTick())
				return 0;
			else return 1;
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