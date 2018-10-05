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
import java.util.Vector;
import java.util.logging.Level;

import com.vividsolutions.jts.geom.Coordinate;

import repast.simphony.engine.schedule.ScheduledMethod;
import repastcity3.agent.IAgent;
import repastcity3.environment.food.DefaultFoodStock;
import repastcity3.environment.food.Food;
import repastcity3.environment.food.FoodOrder;
import repastcity3.environment.food.Nutrition;
import repastcity3.environment.food.FoodEntry;
import repastcity3.environment.food.ProductionList;

import repastcity3.exceptions.NoIdentifierException;
import repastcity3.main.ContextManager;
import repastcity3.utilities.Helper;

import static repastcity3.main.ContextManager.LOGGER;

/**
 * @author CHAO LUO
 *
 */
public class Farm extends FarmableLocation implements FixedGeography {
	// #type of food
	private int variety;
	// amount of all food
	//private double count;
	private int tick;
	private double score;
	private int score_count;
	private List<Food> productionPlan;
	private PriorityQueue<FoodEntry> productionQueue;
	private HashMap<String,List<FoodEntry>> waste;
	private HashMap<String,Double> stockCount;
	private HashMap<String,Double> stockThreshold;
	public Farm() {
		// double setupCost,double dailyMaintenanceCost, double fund,List<Food> stock
		super(1000, 100, 50000, new HashMap<String,List<FoodEntry>>());
		waste = new HashMap<String,List<FoodEntry>>();
		stockCount = new HashMap<String,Double>();
		this.agents = new ArrayList<IAgent>();
		//this.count = 0;
		
		variety = stock.size();
		this.productionPlan = DefaultFoodStock.getRandomFoodList();
		initStock();
		this.productionQueue = new PriorityQueue<FoodEntry>(new feComparator());
		enqueProductionPlan(this.productionPlan);
	}
	private void enqueProductionPlan(List<Food> plan) {
		for (Food food : plan) {
			FoodEntry fe = new FoodEntry(food);
			productionQueue.add(fe);
		}
	}
	private synchronized void dequeProductionQueue() {
		tick = Helper.getCurrentTick();
		FoodEntry fe = productionQueue.peek();
		while(!productionQueue.isEmpty()) {

			if(fe.getProductionTick() <= tick) {
				productionQueue.poll();
				addStock(fe);
			} else {
				break;
			}
		}
	}
	private void initStock() {
		stockThreshold = new HashMap<String,Double>();
		for (Food food : productionPlan) {
			String name = food.getName();
			String type = food.getType();
			FoodEntry fe = new FoodEntry(food);
			//init stock count
			if(!stockCount.containsKey(type)){
				stockCount.put(type,food.getAmount());
			} else {
				double x = stockCount.get(type) +  food.getAmount();
				stockCount.put(type, x);
			}
			//init threshold
			if(!stockThreshold.containsKey(type)){
				stockThreshold.put(type,food.getAmount());
			} else {
				double x = stockThreshold.get(type) +  3*food.getAmount();
				stockThreshold.put(type, x);
			}
			// add to stock
			addStock(fe);
		}
	}
	private void addStock(FoodEntry fe) {
		Food food = fe.getFood();
		String name = food.getName();
		List<FoodEntry> list;
		if (stock.containsKey(name)) {
			list = stock.get(name);
			list.add(new FoodEntry(food));
			stock.put("name", list);
		} else {
			list = new ArrayList<FoodEntry>();
			list.add(new FoodEntry(food));
			stock.put(name, list);
		}
	}
	private void addWaste(FoodEntry fe) {
		Food food = fe.getFood();
		String name = food.getName();
		List<FoodEntry> list;
		if (waste.containsKey(name)) {
			list = waste.get(name);
			list.add(new FoodEntry(food));
			waste.put(name, list);
		} else {
			list = new ArrayList<FoodEntry>();
			list.add(new FoodEntry(food));
			waste.put(name, list);
		}
	}
	private void refreshProductionQueue() {
		// make new production plan and insert them to productionQueue
		DefaultFoodStock.getRandomFoodList();
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
		enqueProductionPlan(foodList);
		return;
	}
	public HashMap<String,List<Food>> getStock(){
		HashMap<String,List<Food>> availableStock=new HashMap<String,List<Food>>();
		for (String type : stock.keySet()) {
			List<FoodEntry> fes = stock.get(type);
			List<Food> list = new ArrayList<Food>();
			for (FoodEntry fe:fes) {
				Food food = fe.getFood();
				if (availableStock.containsKey(type)) {
					list = availableStock.get(type);
					list.add(food);
					availableStock.put(type,list);

				} else {
					list = new ArrayList<Food>();
					list.add(food);
					availableStock.put(type, list);
				}
			}
			/*availableStock.put(type, list);*/
		}
		return availableStock;
	}
	public HashMap<String,List<FoodEntry>> getRawStock(){
		return this.stock;
	}
	@Override
	public void produce() {

		// TODO Auto-generated method stub

	}
	public void checkStock() {
		for (String name : stock.keySet()) {
			List<FoodEntry> fes = stock.get(name);
			Iterator<FoodEntry> iter =  fes.iterator();
			while(iter.hasNext()) {
				FoodEntry fe = (FoodEntry) iter.next();
				fe.check(Helper.getCurrentTick());
				if(fe.expired()) {
					fes.remove(fe);
					addWaste(fe);
				}
			}
			stock.put(name, fes);
		}
	}
	@Override
	public void step() {
		tick=Helper.getCurrentTick();
		if(tick%144==30)
		{
			checkStock();
			refreshProductionQueue();
			dequeProductionQueue();
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

	//private void addFood(Food food) {
	//this.stock.add(food);
	//this.count += food.getAmount();
	//}
	/*
	public double getCount() {
		return count;
	}
	public void setCount(double count) {
		this.count = count;
	}
*/
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
	/*
	@Override
	public synchronized void produce() {
		/
		  TODO: use strategy for production (use preference list)
		 /
		// LOGGER.log(Level.INFO,"Farm "+this.identifier+" is producting");
		for (Food food : stock) {
			if (fund > 0) {
				double amount = food.getAmount();
				double foodCost = food.getProductionCost();
				// produce
				double productionAmount = (1 / food.getProductionTime()) * 144 * scale;
				// strategy: leave some fund to maintain daily business
				if (productionAmount * foodCost > fund) {
					break;
				}
				amount += productionAmount;
				//count += productionAmount;
				productionList.addFood(food, productionAmount,tick);
				food.setAmount(amount);
			} else {
				// strategy: if there is no fund for production, then stop;
				break;
			}
		}
	}

	public void wasteProcess() {

		for (ProductionList.FoodEntry foodEntry:productionList.getList()) {
			if(foodEntry.checkExpired(tick))
			{

			}
		}
	}
	*/
	public boolean isAvailable() {
		return stock.keySet().size() > 0;
	}

	public synchronized void sell(FoodOrder order) {
		HashMap<Food, Double> list = order.getList();
		double totalIncome = this.fund;
		list.forEach((food, amount) -> {
			food.setAmount(food.getAmount() - amount);
			this.fund += amount * food.getPrice();
			//count -= amount;
		});
		totalIncome = this.fund - totalIncome;

		try {
			ContextManager.dLogger.recordSale(order, Helper.getCurrentTick(), totalIncome, this.identifier);
		} catch (IOException e) {
			e.printStackTrace();
		}
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

}