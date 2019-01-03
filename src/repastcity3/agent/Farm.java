/**
 *
 */
package repastcity3.agent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import com.vividsolutions.jts.geom.Coordinate;

import repastcity3.environment.SaleLocation;
import repastcity3.environment.food.Food;
import repastcity3.environment.food.FoodOrder;
import repastcity3.environment.food.Waste;
import repastcity3.main.AgentControl;
import repastcity3.main.ContextManager;
import repastcity3.utilities.Helper;
import repastcity3.utilities.dataUtility.FarmType;
import repastcity3.utilities.ioUtility.FoodUtility;

public class Farm extends SaleLocation {
	// #type of food
	// amount of all food
	// private double count;
	static final double constDis = 1000;
	private List<FarmType> productionTypes;
	private double score;
	private int score_count;
	private PriorityQueue<Food> productionQueue;
	private HashMap<String, Double> stockCount; // count calory of each food category
	private double area;
	private static int uniqueID = 0;
	private double tech;
	private double capacity;
	private double priceFactor;
	private double totalCost;
	private double totalEnergyCost;
	private double totalIncome;
	private double totalDiliveryCost;

	public Farm(double tech, double capacity, double priceFactor,double area, List<FarmType> productionTypes) {
		// double setupCost,double dailyMaintenanceCost, double fund,List<Food> stock
		super(1000, 100, 50000);
		System.out.println("call constructor");
		this.identifier = Integer.toString(uniqueID++);
		this.area=area;
		this.productionTypes = productionTypes;
		this.tech = tech;
		this.capacity = capacity;
		this.priceFactor = priceFactor;
		this.totalCost = 0;
		this.totalEnergyCost = 0;
		this.totalDiliveryCost = 0;
		waste = new HashMap<String, List<Waste>>();

		stockCount = new HashMap<String, Double>();
		this.agents = new ArrayList<IAgent>();
		// this.count = 0;

		// this.productionPlan = FoodUtility.getRandomFoodList(300000, 700000);

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

	}

	private void enqueProductionPlan(List<Food> plan) {
		for (Food food : plan) {
			food.setProductionTick((int) (food.getProductionTick() * tech) + 1);
			productionQueue.add(food);
			System.out.println("food: " + food.getName() + "  amount: " + food.getAmount() + " Ecost: "
					+ food.getEnergyCost() + " cost: " + food.getProductionCost());
			System.out.println("tech:" + tech);
			totalCost += food.getAmount() * food.getProductionCost() * tech;
			totalEnergyCost += (getTotalEnergyCost() + food.getEnergyCost() * food.getAmount() * tech);
			System.out.println("total:" + totalCost + "  total E: " + totalEnergyCost);
		}
	}

	private void dequeProductionQueue() {
		int tick = Helper.getCurrentTick();
		while (!productionQueue.isEmpty()) {

			Food food = productionQueue.peek();
			if (food.getProductionTick() <= tick) {
				productionQueue.poll();
				food.setProductionTick(tick);
				food.setSource(this.toString());
				food.setPrice(priceFactor * food.getValue());
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

		refreshProductionQueue();
	}

	private void addStock(Food food) {
		// //System.out.println(this.toString() + " add to stock " + food.getName() + "
		// "
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

	private void addWaste(Food f) {
		String type = f.getType();
		double stockNum = stockCount.get(type);
		stockCount.put(type, stockNum - f.getAmount());
		Waste wst = new Waste(f);
		// Food food = fe.getFood();

		List<Waste> list;
		if (waste.containsKey(type)) {
			list = waste.get(type);
			list.add(wst);
			// waste.put(name, list);
		} else {
			list = new ArrayList<Waste>();
			list.add(wst);
			waste.put(type, list);
		}
	}

	private void refreshProductionQueue() {

		int tick = Helper.getCurrentTick();
		int t;
		Random r = new Random();
		List<Food> foodList = FoodUtility.getLargeFarmFoodList();
		List<Food> list = new ArrayList<>();
		int len = foodList.size();
		for (int i = 0; i < len; i++) {
			Food f = foodList.get(i);
			t = r.nextInt(productionTypes.size());
			//// System.out.println("i"+i);
			FarmType pt = productionTypes.get(t);
			f.setProductionTick(tick + pt.getPeriod() * 7);
			f.setAmount(pt.getDensity() * area * 1000);
			f.setPrice(pt.getPrice());
			list.add(f);
		}
		enqueProductionPlan(list);

		return;
	}

	public HashMap<String, List<Food>> getStock() {
		return stock;
	}

	public void checkExpiredStock() {
//		//System.out.println(this.toString()+"check stock"		);
		int tick = Helper.getCurrentTick();
		for (String name : stock.keySet()) {
			List<Food> foods = stock.get(name);
			for (int i = 0; i < foods.size(); i++) {
				Food food = foods.get(i);
//					//System.out.println(this.toString()+ "  check stock @ " + Helper.getCurrentTick() + "  "+food.getName() +" @ "+ food.getProductionTick() +"exp time:"+ food.getExpireTime());
				food.check(tick);
				if (food.expired()) {
					foods.remove(food);
					addWaste(food);
				}
			}
			// stock.put(name, foods);
		}

	}

	public void checkCapacityStock() {
		List<Food> fList = stock.get("vegetable");

		int length = fList.size();
		for (int i = length - 1; i >= 0; i--) {
			Food f = fList.get(i);
			if (stockCount.get("vegetable") > capacity) {
				fList.remove(f);
				addWaste(f);
			} else {
				break;
			}
		}
	}

	@Override
	public void step() {
		// System.out.println("farm "+this.id+" start");
		// int tick = Helper.getCurrentTick();
		checkExpiredStock();
		// if (tick % 30 == 0) {
		// refreshProductionQueue();
		// }
		dequeProductionQueue();
		// printStock();
		if (productionQueue.size() == 0) {
			refreshProductionQueue();
		}
		// System.out.println("farm "+this.id+" end");

	}

	public void updateScore(double newScore) {
		this.score_count += 1;
		double score_sum = this.score + newScore;
		this.score = score_sum / score_count;
	}

	public double getScore() {
		return this.score;
	}

	public HashMap<String, List<Waste>> getWaste() {
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

	public void sell(FoodOrder order, Supermarket s) {
		HashMap<Food, Double> list = order.getList();
		double income = this.fund;
		list.forEach((food, amount) -> {
			String type = food.getType();
			double newAmount = food.getAmount() - amount;
			if (newAmount > 0) {
				food.setAmount(newAmount);
			} else {
				stock.get(type).remove(food);
			}
			this.fund += amount * food.getPrice(); // here price is "$ per g"
			this.totalDiliveryCost += this.getDistance(s) * amount;
			double stockNum = stockCount.get(type);
			stockCount.put(type, stockNum - amount);

			// food.setSource(this.toString());
			// count -= amount;
		});

		income += this.fund - income;
		totalIncome += income;

		synchronized (ContextManager.dLogger) {
			try {
				// //System.out.println("====================================");
				// //System.out.println("recordSale!" + " order size:" +
				// order.getList().keySet().size());
				ContextManager.dLogger.recordSale(order, Helper.getCurrentTick(), income, this.toString(),
						s.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// let order be collected by GC
		order = null;
	}

	public double getDistance(Supermarket supermarket) {
		Coordinate origin = AgentControl.getAgentGeometry(this).getCoordinate();
		Coordinate destination = AgentControl.getAgentGeometry(supermarket).getCentroid().getCoordinate();
		double dis = (origin.x - destination.x) * (origin.x - destination.x)
				+ (origin.y - destination.y) * (origin.y - destination.y);
		return dis * constDis;
	}

	public double getDiliveryCost() {
		return this.totalDiliveryCost;
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

	public double getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(double totalCost) {
		this.totalCost = totalCost;
	}

	public double getTotalEnergyCost() {
		return totalEnergyCost;
	}

	public void setTotalEnergyCost(double totalEnergyCost) {
		this.totalEnergyCost = totalEnergyCost;
	}

	public double getTotalIncome() {
		return totalIncome;
	}

}