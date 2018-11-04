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
import repastcity3.environment.food.FoodOrder;
import repastcity3.environment.food.FoodEntry;
import repastcity3.main.ContextManager;
import repastcity3.utilities.Helper;

/**
 * @author CHAO LUO
 *
 */
public class Supermarket extends FarmableLocation implements FixedGeography{

	private int tick;
	private double score;
	private int score_count;
	private List<Food> purchasePlan;
	//private PriorityQueue<FoodEntry> productionQueue;
	private HashMap<String,List<FoodEntry>> waste;
	private HashMap<String,Double> stockCount;
	private HashMap<String,Double> stockThreshold;
	private HashMap<String,Double> vaguePurchasePlan;
	public Supermarket() {
		// double setupCost,double dailyMaintenanceCost, double fund,List<Food> stock
		super(1000, 100, 50000, new HashMap<String,List<Food>>());
		waste = new HashMap<String,List<FoodEntry>>();
		stockCount = new HashMap<String,Double>();
		vaguePurchasePlan = new HashMap<String,Double>();
		this.agents = new ArrayList<IAgent>();
		//this.count = 0;
		this.purchasePlan = DefaultFoodStock.getRandomFoodList();
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
			addStock(food);
		}
		HashMap<String,List<Food>> astock = getStock();
		for (String t : astock.keySet()) {
			// System.out.println("supermarket"+identifier+"has food in "+ t+":  "+astock.get(t).size());
		}
	}
	private void addStock(Food food) {
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
	private void refreshPurchasePlan() {
		// make new production plan and insert them to productionQueue
		DefaultFoodStock.getRandomFoodList();
		Set<String> types = stock.keySet();
		List<String> typeList = new ArrayList<String>();
		List<Food> foodList = new ArrayList<Food>();
		for (String type : types) {
			if(stockCount.get(type) < stockThreshold.get(type)) {
				int totalAmount = 0;
				typeList.add(type);
				while (totalAmount < 2*stockThreshold.get(type)) {
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
		Set<String> types = stockCount.keySet();
		for(String type:types) {
			double base = stockCount.get(type);
			double target = stockThreshold.get(type);
			if(stockCount.get(type)<stockThreshold.get(type)) {
				double amount = 2*target-base;
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
	private void vaguePurchase() {
		/* purchase food according to plan
		 * the loop ends when need is satisfied or there's no other farm to go
		 */
		Iterator<Farm> iter = ContextManager.farmContext.iterator();
			while(iter.hasNext()) { // loop through all farms
				FoodOrder fo = new FoodOrder();
				Farm f = iter.next();
				HashMap<String,List<Food>> fStock = f.getStock();
				for(String type:fStock.keySet()) { //loop through all kinds of stock of a farm
					double target = vaguePurchasePlan.get(type);
					List<Food> foodOfType= fStock.get(type);
					Iterator iter2 = foodOfType.iterator();
					while(iter2.hasNext()) {
						if (target > 0.0) {
							Food fd = (Food) iter2.next();
							fo.addOrder(fd,fd.getAmount());
							addStock(fd);
							target = target - fd.getAmount();
							if (target <0.0)
								target = 0.0;
							vaguePurchasePlan.put(type, target);
						} else {
							break;
						}
					}
					
				}
				System.out.println("vague purchase fo size: "+fo.getList().size() );
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
		System.out.println(this.toString() +"  check stock");
		// move expired food to waste
		for (String name : stock.keySet()) {
			List<Food> fes = stock.get(name);
			Iterator<Food> iter =  fes.iterator();
			while(iter.hasNext()) {
				Food fe = (Food) iter.next();
				fe.check(Helper.getCurrentTick());
				if(fe.expired()) {
					fes.remove(fe);
					addWaste(new FoodEntry(fe));
				}
			}
			stock.put(name, fes);
		}
	}
	@Override
	public void step() {
		System.out.println("Supermarket" + this.toString() + "step");
		tick=Helper.getCurrentTick();
		checkStock();
		if(tick%144==30)
		{
			
			refreshPurchasePlan();
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
			if(amount >0) {
				food.setAmount(food.getAmount() - amount);
			} else {
				List<Food> stockList = stock.get(food.getType());
				stockList.remove(food);
			}
			stockCount.put(type, newAmount);
			this.fund += amount * food.getPrice();
			//count -= amount;
		});
		totalIncome = this.fund - totalIncome;

		try {
			ContextManager.dLogger.recordSale(order, Helper.getCurrentTick(), totalIncome, this.toString(),consumerID);
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
