/**
 *
 */
package repastcity3.agent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;


import repastcity3.environment.SaleLocation;
import repastcity3.environment.food.FoodUtility;
import repastcity3.environment.food.Food;
import repastcity3.environment.food.FoodEntry;
import repastcity3.environment.food.FoodOrder;
import repastcity3.main.ContextManager;
import repastcity3.utilities.Helper;
import repastcity3.utilities.dataUtility.ProductionType;
import repastcity3.utilities.ioUtility.*;

public class Farm extends SaleLocation {
	// #type of food
	// amount of all food
	// private double count;
	private List<ProductionType> productionTypes; 
	private double score;
	private int score_count;
	private List<Food> productionPlan;
	private PriorityQueue<Food> productionQueue; 
	private HashMap<String, Double> stockCount; // count calory of each food category
	private HashMap<String, Double> stockThreshold; // threshold for each food category
	private int area;
	private static int uniqueID = 0;
	private int id;

	public Farm() {
		// double setupCost,double dailyMaintenanceCost, double fund,List<Food> stock
		
		super(1000, 100, 50000);
		
		Random r = new Random();
		int type = r.nextInt(3);
		if(type == 0) {
			area = 5000;
		} else if(type == 1) {
			area = 1000;
		} else if(type == 2){
			area = 2;
		}
		productionTypes = DataLoader.loadProductionType() ;
		System.out.println(this.toString()+"init");
		for (ProductionType pt:productionTypes) {
			System.out.println(pt.toString());
		}
		this.id = uniqueID++;
		waste = new HashMap<String, List<FoodEntry>>();
		stockCount = new HashMap<String, Double>();
		this.agents = new ArrayList<IAgent>();
		// this.count = 0;
		
		//this.productionPlan = FoodUtility.getRandomFoodList(300000, 700000);
		
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
		initStock();
		//enqueProductionPlan(this.productionPlan);
		// System.out.println("init farm " + identifier + " with stockCount " +
		// stock.keySet().size());
	}

	private void enqueProductionPlan(List<Food> plan) {
		for (Food food : plan) {
			productionQueue.add(food);
		}
	}

	private void dequeProductionQueue() {
		int tick = Helper.getCurrentTick();
		while (!productionQueue.isEmpty()) {
			
			Food food = productionQueue.peek();
			if (food.getProductionTick() <= tick){
				productionQueue.poll();
				food.setProductionTick(tick);
				food.setSource(this.toString());
				this.addStock(food);
				countStock(food);

			} else {
				break;
			}
		}

	}
	private void countStock(Food food) {
		String type = food.getType();
		if (!stockCount.containsKey(type)) {
			stockCount.put(type, food.getAmount());
		} else {
			double x = stockCount.get(type) + food.getAmount();
			stockCount.put(type, x);
		}
	}
	private void initStock() {
		/*
		stockThreshold = new HashMap<String, Double>();
		for (Food food : productionPlan) {
			String type = food.getType();
			food.setProductionTick(0);
			// init stock count
			
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
			// System.out.println(t + " has " + astock.get(t).size());
		}
		*/
		refreshProductionQueue();
	}

	private void addStock(Food food) {
		// System.out.println(this.toString() + " add to stock " + food.getName() + " "
		// + food.getAmount());
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
		// Food food = fe.getFood();
		String type = fe.getType();
		List<FoodEntry> list;
		if (waste.containsKey(type)) {
			list = waste.get(type);
			list.add(fe);
			// waste.put(name, list);
		} else {
			list = new ArrayList<FoodEntry>();
			list.add(fe);
			waste.put(type, list);
		}
	}

	private void refreshProductionQueue() {
		// make new production plan and insert them to productionQueue
		/*
		FoodUtility.getRandomFoodList(300000, 700000);
		Set<String> types = stock.keySet();
		List<String> typeList = new ArrayList<String>();
		List<Food> foodList = new ArrayList<Food>();
		for (String type : types) {
			if (stockCount.get(type) < stockThreshold.get(type)) {
				int totalAmount = 0;
				typeList.add(type);
				while (totalAmount < stockThreshold.get(type)) {
					Food f = FoodUtility.getFoodByType(type);
//					System.out.print("get random food  " + f.getAmount());
					totalAmount += f.getAmount();
					f.setProductionTick(Helper.getCurrentTick() + f.getProductionTime());
					foodList.add(f);
				}
			}
		}*/
		int tick = Helper.getCurrentTick();
		int t;
		Random r = new Random();
		List<Food> foodList = FoodUtility.getLargeFarmFoodList();
		List<Food> list = new ArrayList<>();
		int len = foodList.size();
		for (int i = 0; i < len; i++) {
			Food f = foodList.get(i);
			t=r.nextInt(productionTypes.size());
			System.out.println("i"+i);
			ProductionType pt = productionTypes.get(t);
			f.setProductionTick(tick+pt.getPeriod()*7);
			f.setAmount(pt.getDensity()*area*1000);
			f.setPrice(pt.getPrice());
			list.add(f);
		}
		enqueProductionPlan(list);

		return;
	}

	public HashMap<String, List<Food>> getStock() {
		return stock;
	}

	public void checkStock() {
//		System.out.println(this.toString()+"check stock"		);
		int tick = Helper.getCurrentTick();
		for (String name : stock.keySet()) {
			List<Food> foods = stock.get(name);
			for (int i = 0; i < foods.size(); i++) {
				Food food = foods.get(i);
//					System.out.println(this.toString()+ "  check stock @ " + Helper.getCurrentTick() + "  "+food.getName() +" @ "+ food.getProductionTick() +"exp time:"+ food.getExpireTime());
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
		System.out.println("farm "+this.id+" start");
		//int tick = Helper.getCurrentTick();
		checkStock();
		//if (tick % 30 == 0) {
			//refreshProductionQueue();
		//}
		dequeProductionQueue();
		// printStock();
		if(productionQueue.size() == 0) {
			refreshProductionQueue();
		}
		System.out.println("farm "+this.id+" end");

	}

	public void updateScore(double newScore) {
		this.score_count += 1;
		double score_sum = this.score + newScore;
		this.score = score_sum / score_count;
	}

	public double getScore() {
		return this.score;
	}

	public HashMap<String, List<FoodEntry>> getWaste() {
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

	public void sell(FoodOrder order, String sID) {
		HashMap<Food, Double> list = order.getList();
		double totalIncome = this.fund;
		list.forEach((food, amount) -> {
			String type = food.getType();
			double newAmount = food.getAmount() - amount;
			if (newAmount > 0) {
				food.setAmount(newAmount);
			} else {
				stock.get(type).remove(food);
			}
			this.fund += amount * food.getPrice(); //here price is "$ per g"
			double stockNum = stockCount.get(type);
			stockCount.put(type, stockNum - amount);
			//food.setSource(this.toString());
			// count -= amount;
		});

		totalIncome = this.fund - totalIncome;
		synchronized(ContextManager.dLogger) {
			try {
				// System.out.println("====================================");
				// System.out.println("recordSale!" + "   order size:" + order.getList().keySet().size());
			ContextManager.dLogger.recordSale(order, Helper.getCurrentTick(),
			totalIncome, this.toString(),sID);
			} catch (IOException e) {
			e.printStackTrace();
			}
		}

		// let order be collected by GC
		order = null;
	}

	private void printStock() {
		System.out.println(this.toString() + "  print stock");
		for (String type : stock.keySet()) {
			System.out.println(type);
			int len = stock.get(type).size();
			for (int i = 0; i < len; i++) {
				Food f = stock.get(type).get(i);
				System.out.println(
						this.toString() + "  " + f.getName() + "  " + f.getAmount() + " @" + f.getProductionTick());
			}
		}

	}

}