/**
 *
 */
package repastcity3.environment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import repastcity3.agent.IAgent;
import repastcity3.environment.food.DefaultFoodStock;
import repastcity3.environment.food.Food;
import repastcity3.environment.food.FoodEntry;
import repastcity3.environment.food.FoodOrder;
import repastcity3.main.ContextManager;
import repastcity3.utilities.Helper;

/**
 * @author CHAO LUO
 *
 */
public class Farm extends FarmableLocation implements FixedGeography {
	// #type of food
	private int variety;
	// amount of all food
	// private double count;
	private int tick;
	private double score;
	private int score_count;
	private List<Food> productionPlan;
	private PriorityQueue<Food> productionQueue;
	private HashMap<String, List<FoodEntry>> waste;
	private HashMap<String, Double> stockCount;
	private HashMap<String, Double> stockThreshold;

	public Farm() {
		// double setupCost,double dailyMaintenanceCost, double fund,List<Food> stock
		super(1000, 100, 50000, new HashMap<String, List<Food>>());
		waste = new HashMap<String, List<FoodEntry>>();
		stockCount = new HashMap<String, Double>();
		this.agents = new ArrayList<IAgent>();
		// this.count = 0;

		variety = stock.size();
		this.productionPlan = DefaultFoodStock.getRandomFoodList(300000,700000);
		initStock();
		this.productionQueue = new PriorityQueue<Food>(new Comparator<Food>() {
			public int compare(Food f1, Food f2) {

				if (f1.productionTick < f2.productionTick)
					return -1;
				else if (f1.productionTick == f2.productionTick)
					return 0;
				else
					return 1;
			}
		});
		enqueProductionPlan(this.productionPlan);
		// System.out.println("init farm " + identifier + " with stockCount " + stock.keySet().size());
	}

	private void enqueProductionPlan(List<Food> plan) {
		for (Food food : plan) {
			productionQueue.add(food);
		}
	}

	private void dequeProductionQueue() {
		// food produced is add to stock
		//System.out.println();
		//System.out.println(this.toString() + " call deque");
		while (!productionQueue.isEmpty()) {
			Food food = productionQueue.peek();
			//System.out.println("deque @ " + Helper.getCurrentTick() + " pTick: " + food.getProductionTick());
			if (food.getProductionTick() <= tick) {
				productionQueue.poll();
				//food.setSource(this.toString());
				this.addStock(food);
				
			} else {
				break;
			}
		}

	}

	private void initStock() {
		stockThreshold = new HashMap<String, Double>();
		for (Food food : productionPlan) {
			String type = food.getType();
			food.setProductionTick(0);
			// init stock count
			if (!stockCount.containsKey(type)) {
				stockCount.put(type, food.getAmount());
			} else {
				double x = stockCount.get(type) + food.getAmount();
				stockCount.put(type, x);
			}
			// init threshold
			if (!stockThreshold.containsKey(type)) {
				stockThreshold.put(type, food.getAmount());
			} else {
				double x = stockThreshold.get(type) + 0.5 * food.getAmount();
				stockThreshold.put(type, x);
			}
			// add to stock
			addStock(food);
		}
		HashMap<String, List<Food>> astock = getStock();
		for (String t : astock.keySet()) {
			// System.out.println(t + "  has " + astock.get(t).size());
		}
	}

	private void addStock(Food food) {
		//System.out.println(this.toString() + "  add to stock  " + food.getName() + "  " + food.getAmount());
		String type = food.getType();
		List<Food> list;
		if (stock.containsKey(type)) {
			list = stock.get(type);
			list.add(food);
		} else {
			list = new ArrayList<Food>();
			list.add(food);
			stock.put(type, list);
		}
	}

	private void addWaste(FoodEntry fe) {
		//Food food = fe.getFood();
		String type = fe.getType();
		List<FoodEntry> list;
		if (waste.containsKey(type)) {
			list = waste.get(type);
			list.add(fe);
			//waste.put(name, list);
		} else {
			list = new ArrayList<FoodEntry>();
			list.add(fe);
			waste.put(type, list);
		}
	}

	private void refreshProductionQueue() {
		// make new production plan and insert them to productionQueue
		DefaultFoodStock.getRandomFoodList(300000,700000);
		Set<String> types = stock.keySet();
		List<String> typeList = new ArrayList<String>();
		List<Food> foodList = new ArrayList<Food>();
		for (String type : types) {
			if (stockCount.get(type) < stockThreshold.get(type)) {
				int totalAmount = 0;
				typeList.add(type);
				while (totalAmount < stockThreshold.get(type)) {
					Food f = DefaultFoodStock.getFoodByType(type);
					System.out.print("get random food  " + f.getAmount());
					totalAmount += f.getAmount();
					f.setProductionTick(Helper.getCurrentTick()+f.getProductionTime());
					foodList.add(f);
				}
			}
		}
		enqueProductionPlan(foodList);
		return;
	}

	public HashMap<String, List<Food>> getStock() {
		return stock;
	}

	public void checkStock() {
//		System.out.println(this.toString()+"check stock"		);
		int tick=Helper.getCurrentTick();
		synchronized (this) {
			for (String name : stock.keySet()) {
				List<Food> foods = stock.get(name);
				for (int i = 0; i < foods.size(); i++) {
					Food food=foods.get(i);
//					System.out.println(this.toString()+ "  check stock @ " + Helper.getCurrentTick() + "  "+food.getName() +" @ "+ food.getProductionTick() +"exp time:"+ food.getExpireTime());
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
		tick = Helper.getCurrentTick();
		checkStock();
		if (tick % 30 == 0) {
			refreshProductionQueue();
		}
		dequeProductionQueue();
		//printStock();

	}

	public void updateScore(double newScore) {
		this.score_count += 1;
		double score_sum = this.score + newScore;
		this.score = score_sum / score_count;
	}

	public double getScore() {
		return this.score;
	}
	public HashMap<String,List<FoodEntry>> getWaste(){
		return this.waste;
	}

	@Override
	public boolean isThreadable() {
		return true;
	}

	@Override
	public String toString() {
		return "Farm: " + this.identifier;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Farm))
			return false;
		Farm b = (Farm) obj;
		return this.identifier.equals(b.identifier);
	}

	public boolean isAvailable() {
		return stock.keySet().size() > 0;
	}

	public void sell(FoodOrder order,String sID) {
		HashMap<Food, Double> list = order.getList();
		double totalIncome = this.fund;
		synchronized (this) {
			list.forEach((food, amount) -> {
				String type = food.getType();
				double newAmount = food.getAmount()-amount;
				if(newAmount > 0) {
					food.setAmount(newAmount);	
				} else {
					stock.get(type).remove(food);
				}
				this.fund += amount * food.getPrice();
				double stockNum = stockCount.get(type);
				stockCount.put(type, stockNum-amount);
				food.setSource(this.toString());
				// count -= amount;
			});
		}

		totalIncome = this.fund - totalIncome;

		try {
			// System.out.println("====================================");
			// System.out.println("recordSale!" + "   order size:" + order.getList().keySet().size());
		ContextManager.dLogger.recordSale(order, Helper.getCurrentTick(),
		totalIncome, this.toString(),sID);
		} catch (IOException e) {
		e.printStackTrace();
		}

		// let order be collected by GC
		order = null;
	}
	private  void printStock() {
		System.out.println(this.toString()+"  print stock");
		 synchronized(this) {
		for (String type:stock.keySet()) {
			System.out.println(type);
			int len =stock.get(type).size();
			for(int i = 0; i < len; i++) {
				Food f = stock.get(type).get(i);
				System.out.println(this.toString() + "  " + f.getName() + "  " + f.getAmount() + " @"+ f.getProductionTick());
			}
		}
	 }
	}
	

}