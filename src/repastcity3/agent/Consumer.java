package repastcity3.agent;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import repastcity3.environment.Residential;

import repastcity3.environment.Route;
import repastcity3.environment.School;
import repastcity3.environment.Supermarket;
import repastcity3.environment.Workplace;
import repastcity3.environment.food.Food;
import repastcity3.environment.food.FoodOrder;
import repastcity3.main.AgentControl;
import repastcity3.main.ContextManager;
import repastcity3.utilities.Helper;

public class Consumer implements People {

	private static Logger LOGGER = Logger.getLogger(Consumer.class.getName());

	private Residential home; // Where the agent lives
	private Route route; // An object to move the agent around the world
	private Double distance;

	private Coordinate origin;
	private Coordinate destination;
	private Farm farm;
	private Residential residential;

	private boolean goforEat = false;
	private double healthThreshold;
	
	private double default_health = 1000;
	private double default_consumption_rate = 5;
	private double stockThreshold = 1000;
	private double caloryConsumption = 0;
	private double caloryProduction;
	private double satisfaction;
	private double avg_satisfaction;
	private int buy_time;
	private Preference preference = null;
	private static int uniqueID = 0;

	private double thredis = 2; // In kilometer;
	private int id;

	private int type = 3;

	private Income income;
	private Double consumption_rate;
	private HashMap<String, List<Food>> consumer_food_stock;

	private Catagory catagory;
	private Gender gender;
	

	public Consumer(Catagory catagory, Gender gender, Income income, Double consumption_rate) {
		this.id = uniqueID++;
		this.preference = new Preference();
		this.consumer_food_stock = new HashMap<String, List<Food>>();
		this.avg_satisfaction = 0;
		this.buy_time = 0;
		this.residential = ContextManager.residentialContext.getRandomObject();
		// -------------------
		this.catagory = catagory;
		this.preference = preference;
		this.consumption_rate = consumption_rate;
		this.gender = gender;
		this.income = income;
		// -----------
		setPurpose();
	}

	public Consumer() {
		this.id = uniqueID++;
		this.preference = new Preference();
		this.consumer_food_stock = new HashMap<String, List<Food>>();
		Random random = new Random();
		this.residential = ContextManager.residentialContext.getRandomObject();
		int c = random.nextInt(100);

		// if (c <= 7) {
		// catagory = CATAGORY.CHILD;
		// setgender();
		// this.health = random.nextInt(50) + 200;
		// this.mealEnergy = random.nextInt(50) + 300;
		// setHealth(mealEnergy);
		// File jsonFile =
		// Paths.get("./data/agent_data/preference_child.json").toFile();
		// setPreference(jsonFile);
		// } else if (c > 7 && c <= 17) {
		// catagory = CATAGORY.TEENAGER;
		// setgender();
		// this.health = random.nextInt(100) + 200;
		// this.mealEnergy = random.nextInt(100) + 300;
		// setHealth(mealEnergy);
		// File jsonFile =
		// Paths.get("./data/agent_data/preference_teenager.json").toFile();
		// setPreference(jsonFile);
		// } else if (c > 17 && c <= 97) {
		// catagory = CATAGORY.ADULTS;
		// setgender();
		// this.health = random.nextInt(150) + 200;
		// this.mealEnergy = random.nextInt(150) + 300;
		// setHealth(mealEnergy);
		// File jsonFile =
		// Paths.get("./data/agent_data/preference_adults.json").toFile();
		// setPreference(jsonFile);
		// } else {
		// catagory = CATAGORY.OLD;
		// setgender();
		// this.health = random.nextInt(200) + 200;
		// this.mealEnergy = random.nextInt(200) + 300;
		// setHealth(mealEnergy);
		// File jsonFile = Paths.get("./data/agent_data/preference_old.json").toFile();
		// setPreference(jsonFile);
		// }
		this.avg_satisfaction = 0;
		this.buy_time = 0;
		setPurpose();
	}

	public void setHealth(int mealEnergy) {
		this.healthThreshold = 0.5 * mealEnergy;
		this.caloryConsumption = mealEnergy / 50;
	}

	public void setPreference(File jsonFile) {

		/*
		 * Gson gson = new Gson(); try { JsonObject jsonObject = gson.fromJson(new
		 * FileReader(jsonFile), JsonObject.class); preference.put("carbohydrate",
		 * jsonObject.get("carbohydrate").getAsDouble()); preference.put("protein",
		 * jsonObject.get("protein").getAsDouble()); preference.put("lipid",
		 * jsonObject.get("lipid").getAsDouble()); preference.put("water",
		 * jsonObject.get("water").getAsDouble()); preference.put("vitamins",
		 * jsonObject.get("vitamins").getAsDouble()); preference.put("minerals",
		 * jsonObject.get("minerals").getAsDouble()); } catch
		 * (JsonSyntaxException|JsonIOException|FileNotFoundException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */
	}

	public int setPurpose() {
		Random random = new Random();
		this.type = random.nextInt(3) + 1;
		return type;
	}

	@Override
	public void step() throws Exception {
		// System.out.println("consumer "+this.id+" start");
		double stock_calory = getStockCalory(consumer_food_stock);
		
		if (stock_calory < this.stockThreshold) {
			// System.out.println("========"+ this.toString()+"enter==========");
			this.distance = 0.0;
			int[] flags = { 0, 0, 0, 0, 0 };
			TreeMap<Double, Supermarket> supermarketTreeMap = selectSupermarket();
			// System.out.print(supermarketTreeMap.size());
			for (Double key : supermarketTreeMap.keySet()) {
				Supermarket supermarket = supermarketTreeMap.get(key);
				Coordinate o = AgentControl.getAgentGeometry(this).getCoordinate();
				Coordinate d = ContextManager.supermarketProjection.getGeometry(supermarket).getCentroid()
						.getCoordinate();
				this.distance += Math.sqrt((o.x-d.x)*(o.x-d.x)+(o.y-d.y)*(o.y-d.y));
				GeometryFactory geomFac = new GeometryFactory();
				AgentControl.moveAgent(this, geomFac.createPoint(this.destination));
				FoodOrder foodOrder = new FoodOrder();
				if(supermarket.isAvailable())
					foodOrder = this.selectFood(supermarket, flags);
				// System.out.println("----------"+this.toString()+ " buy from " + supermarket.toString());
				supermarket.sell(foodOrder, this.toString());
				supermarket.updateScore(this.satisfaction);
				if (flags[0] == 1 && flags[1] == 1 && flags[2] == 1 && flags[3] == 1 && flags[4] == 1) {
					break;
				}
			}

		} else {
			goRandomPlace(this.type);
			setPurpose();
		}
		if (this.buy_time != 0) {
			this.avg_satisfaction = this.avg_satisfaction / this.buy_time;
		}
		this.default_health -= this.default_consumption_rate*this.consumption_rate;
		consumeRandomFood();
		
		//System.out.println("consumer "+this.id+" end");
	}

	/**
	 * There will be no inter-agent communication so these agents can be executed
	 * simulataneously in separate threads.
	 */
	@Override
	public final boolean isThreadable() {
		return true;
	}

	public void setHome(Residential home) {
		this.home = home;
	}

	public Residential getHome() {
		return this.home;
	}

	@Override
	public <T> void addToMemory(List<T> objects, Class<T> clazz) {
	}

	@Override
	public List<String> getTransportAvailable() {
		return null;
	}

	@Override
	public String toString() {
		return "Consumer " + this.id;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Consumer))
			return false;
		Consumer b = (Consumer) obj;
		return this.id == b.id;
	}

	@Override
	public int hashCode() {
		return this.id;
	}

	public Income getIncome() {
		return income;
	}

	public double getCaloryConsumption() {
		return caloryConsumption;
	}

	public double getCaloryProduction() {
		return caloryProduction;
	}

	public double getHealthThreshold() {
		return this.healthThreshold;
	}

	public void setHealthThreshold(double healthThreshold) {
		this.healthThreshold = healthThreshold;
	}

	public void goRandomPlace(int type) throws Exception {
		switch (type) {
		case 1: {
			School S = ContextManager.schoolContext.getRandomObject();
			// this.route = new Route(this,
			// ContextManager.schoolProjection.getGeometry(S).getCentroid().getCoordinate(),
			// S);
			// this.origin = AgentControl.getAgentGeometry(this).getCoordinate();
			this.destination = ContextManager.schoolProjection.getGeometry(S).getCentroid().getCoordinate();
			GeometryFactory geomFac = new GeometryFactory();
			AgentControl.moveAgent(this, geomFac.createPoint(this.destination));
			break;
		}
		case 2: {
			Workplace W = ContextManager.workplaceContext.getRandomObject();
			// this.route = new Route(this,
			// ContextManager.workplaceProjection.getGeometry(W).getCentroid().getCoordinate(),
			// W);
			// this.origin = AgentControl.getAgentGeometry(this).getCoordinate();
			this.destination = ContextManager.workplaceProjection.getGeometry(W).getCentroid().getCoordinate();
			GeometryFactory geomFac = new GeometryFactory();
			AgentControl.moveAgent(this, geomFac.createPoint(this.destination));
			break;
		}
		case 3: {
			this.destination = ContextManager.residentialProjection.getGeometry(this.residential).getCentroid()
					.getCoordinate();
			GeometryFactory geomFac = new GeometryFactory();
			AgentControl.moveAgent(this, geomFac.createPoint(this.destination));
			break;
		}

		}
	}

	public Farm selectFarm() {
		Random random = new Random();
		Farm farm = null;
		int seed = random.nextInt(100);
		if (seed < 100) {
			farm = findNearestFarm();
		} else {
			farm = findPopularFarm();
		}
		return farm;
	}

	public TreeMap<Double, Supermarket> selectSupermarket() {
		TreeMap<Double, Supermarket> treeMap = new TreeMap<Double, Supermarket>(new Comparator<Double>() {
			@Override
			public int compare(Double o1, Double o2) {
				return o2.compareTo(o1);
			}
		});
		Iterator<Supermarket> iterator = ContextManager.supermarketContext.iterator();
		while (iterator.hasNext()) {
			Supermarket supermarket = iterator.next();
			double score = getSupermarketScore(supermarket);
			/*
			 * try {
			 * System.out.println("======"+supermarket.getIdentifier()+" "+score+"====="); }
			 * catch (NoIdentifierException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); }
			 */
			treeMap.put(score, supermarket);
		}
		return treeMap;
	}

	public double getSupermarketScore(Supermarket supermarket) {
		double d_score = getSupermarketDistanceScore(supermarket);
		// System.out.println("dscore:"+d_score);
		double s_score = getSupermarketRatingScore(supermarket);
		// System.out.println("s_score:"+s_score);
		double d_weight = preference.get_d_weight();
		double s_weight = preference.get_s_weight();
		Random random = new Random();
		double score = d_score * d_weight + s_score * s_weight + random.nextDouble();
		return score;
	}

	public Farm findNearestFarm() {
		Iterator<Farm> iter = ContextManager.farmContext.iterator();
		double min = Double.POSITIVE_INFINITY;
		Farm nearestFarm = null;
		// may not iterate all the farms.
		int iterTime = 100;
		while (iter.hasNext()) {
			Farm farm = iter.next();
			Route r = new Route(this, ContextManager.farmProjection.getGeometry(farm).getCentroid().getCoordinate(),
					farm);
			this.origin = AgentControl.getAgentGeometry(this).getCoordinate();
			this.destination = ContextManager.farmProjection.getGeometry(farm).getCentroid().getCoordinate();
			double dis = (origin.x - destination.x) * (origin.x - destination.x)
					+ (origin.y - destination.y) * (origin.y - destination.y);
			if (dis < min) {
				min = dis;
				nearestFarm = farm;
			}
		}
		return nearestFarm;
	}

	/*
	 * public Supermarket selectSupermarket() {
	 *
	 * }
	 */

	public double getSupermarketDistanceScore(Supermarket supermarket) {
		Iterator<Supermarket> iter = ContextManager.supermarketContext.iterator();
		double min = 10000000;
		double max = -10000000;
		double distance = getDistance(supermarket);
		// System.out.println("distance:"+distance);
		int count = 0;
		while (iter.hasNext()) {
			count++;
			Supermarket s = iter.next();
			double dis = getDistance(s);
			// System.out.println("min:"+min+"dis:"+dis+"max:"+max);

			if (dis < min) {

				min = dis;
			}
			if (dis > max) {
				max = dis;
			}
		}
		// System.out.println("count"+count);
		// System.out.println("max:"+max+"min:"+min);
		double score = 1000 * (distance - min) / (max - min);
		// System.out.println("score:"+score);
		return score;
	}

	public double getSupermarketRatingScore(Supermarket supermarket) {
		Iterator<Supermarket> iter = ContextManager.supermarketContext.iterator();
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		double score = supermarket.getScore();
		while (iter.hasNext()) {
			Supermarket s = iter.next();
			double score2 = s.getScore();
			if (score2 < min) {
				min = score2;
			} else if (score2 > max) {
				max = score2;
			}
		}
		double score3 = 100 * (score - min) / (max - min);
		return score3;
	}

	public double getDistance(Supermarket supermarket) {
		this.origin = AgentControl.getAgentGeometry(this).getCoordinate();
		this.destination = ContextManager.supermarketProjection.getGeometry(supermarket).getCentroid().getCoordinate();
		double dis = (origin.x - destination.x) * (origin.x - destination.x)
				+ (origin.y - destination.y) * (origin.y - destination.y);
		return dis * 1000;
	}

	public Supermarket findNearestSupermarket() {
		Iterator<Supermarket> iter = ContextManager.supermarketContext.iterator();
		double min = Double.POSITIVE_INFINITY;
		Supermarket nearestSupermarket = null;
		// may not iterate all the farms.
		int iterTime = 100;
		while (iter.hasNext()) {
			Supermarket supermarket = iter.next();
			Route r = new Route(this,
					ContextManager.supermarketProjection.getGeometry(supermarket).getCentroid().getCoordinate(),
					supermarket);
			this.origin = AgentControl.getAgentGeometry(this).getCoordinate();
			this.destination = ContextManager.supermarketProjection.getGeometry(supermarket).getCentroid()
					.getCoordinate();
			double dis = (origin.x - destination.x) * (origin.x - destination.x)
					+ (origin.y - destination.y) * (origin.y - destination.y);
			if (dis < min) {
				min = dis;
				nearestSupermarket = supermarket;
			}
		}
		return nearestSupermarket;
	}

	public Farm findPopularFarm() {
		Iterator<Farm> iter = ContextManager.farmContext.iterator();
		double max = 0;
		Farm PopularFarm = null;
		while (iter.hasNext()) {
			Farm farm = iter.next();
			double score = farm.getScore();
			if (score > max) {
				max = score;
				PopularFarm = farm;
			}
		}
		return PopularFarm;
	}

	public Supermarket findPopularSupermarket() {
		Iterator<Supermarket> iter = ContextManager.supermarketContext.iterator();
		double max = 0;
		Supermarket PopularSupermarket = null;
		while (iter.hasNext()) {
			Supermarket supermarket = iter.next();
			double score = supermarket.getScore();
			if (score > max) {
				max = score;
				PopularSupermarket = supermarket;
			}
		}
		return PopularSupermarket;
	}

	/*
	 * double getFoodScore(Food food) { Nutrition ntr = food.getNutrition(); //
	 * to-do: normalize the value of each kind of nutrition (divide the max-min //
	 * value ) double score = preference.get("lipid") * ntr.getlipid() +
	 * preference.get("carbohydrate") * ntr.getCarbohydrate() +
	 * preference.get("protein") * ntr.getprotein() + preference.get("water") *
	 * ntr.getwater() + preference.get("vitamins") * ntr.getvitamins() +
	 * preference.get("minerals") * ntr.getminerals(); return score; }
	 */

	double getStockCalory(HashMap<String, List<Food>> stock) {
		double calory_sum = 0;
		Set<String> key = stock.keySet();
		for (String foodType : key) {
			List<Food> list = stock.get(foodType);
			for (Food f : list) {
				calory_sum += f.getDensity() * f.getAmount();
			}
		}
		return calory_sum;
	}
	/*
	 * public FoodOrder selectFood(Farm farm) { //
	 * System.err.println("enter select food");
	 * 
	 * FoodOrder foodOrder = new FoodOrder(); this.satisfaction = 100;
	 * 
	 * 
	 * synchronized (farm) { HashMap<String, List<Food>> stock = farm.getStock();
	 * Preference preference = new Preference(); List<Food> grain_list =
	 * stock.get("grain"); if (grain_list != null) { //
	 * System.out.println("grain: "+grain_list.size()); HashMap<String, Food>
	 * grain_map = toHashMap(grain_list); ArrayList<String> grain_prefer =
	 * preference.getGrain_list(); buyFood(grain_map, grain_prefer, foodOrder); }
	 * List<Food> vegetable_list = stock.get("vegetable"); if (vegetable_list !=
	 * null) { // System.out.println("vegetable: "+vegetable_list.size());
	 * HashMap<String, Food> vegetable_map = toHashMap(vegetable_list);
	 * ArrayList<String> vegetable_prefer = preference.getVegetable_list();
	 * buyFood(vegetable_map, vegetable_prefer, foodOrder); } List<Food> fruit_list
	 * = stock.get("fruit"); if (fruit_list != null) { //
	 * System.out.println("fruit: "+fruit_list.size()); HashMap<String, Food>
	 * fruit_map = toHashMap(fruit_list); ArrayList<String> fruit_prefer =
	 * preference.getFruit_list(); buyFood(fruit_map, fruit_prefer, foodOrder); }
	 * List<Food> dairy_list = stock.get("dairy"); if (dairy_list != null) { //
	 * System.out.println("dairy: "+dairy_list.size()); HashMap<String, Food>
	 * dairy_map = toHashMap(dairy_list); ArrayList<String> dairy_prefer =
	 * preference.getDairy_list(); buyFood(dairy_map, dairy_prefer, foodOrder); }
	 * 
	 * List<Food> meat_list = stock.get("meat"); if (meat_list != null) { //
	 * System.out.println("meat: "+meat_list.size()); HashMap<String, Food> meat_map
	 * = toHashMap(meat_list); ArrayList<String> meat_prefer =
	 * preference.getMeat_list(); buyFood(meat_map, meat_prefer, foodOrder); } }
	 * 
	 * return foodOrder; }
	 */

	public FoodOrder selectFood(Supermarket supermarket, int[] flags) {
		// System.err.println("enter select food");

		FoodOrder foodOrder = new FoodOrder();
		this.satisfaction = 100;

		HashMap<String, List<Food>> stock = supermarket.getStock();
		// System.out.println("start selecting food, supermarket stock size:" +
		// stock.keySet().size());
		Preference preference = new Preference();
		List<Food> grain_list = stock.get("grain");
		// System.out.println("grain:"+grain_list.size());
		// System.out.println(flags[0]+" "+flags[1]+" "+flags[2]+" "+flags[3]+"
		// "+flags[4]);
		if (grain_list != null && flags[0] == 0) {
			// System.out.println("buy grain: "+grain_list.size());
			HashMap<String, Food> grain_map = toHashMap(grain_list);
			ArrayList<String> grain_prefer = preference.getGrain_list();
			buyFood("grain", grain_map, grain_prefer, foodOrder);
			flags[0] = 1;

		}
		List<Food> vegetable_list = stock.get("vegetable");
		// System.out.println("vegetable:"+vegetable_list.size());
		if (vegetable_list != null && flags[1] == 0) {
			// System.out.println("buy vegetable: "+vegetable_list.size());
			HashMap<String, Food> vegetable_map = toHashMap(vegetable_list);
			ArrayList<String> vegetable_prefer = preference.getVegetable_list();
			buyFood("vegetable", vegetable_map, vegetable_prefer, foodOrder);
			flags[1] = 1;
		}
		List<Food> fruit_list = stock.get("fruit");
		// System.out.println("fruit_list:"+fruit_list.size());
		if (fruit_list != null && flags[2] == 0) {
			// System.out.println("buy fruit: "+fruit_list.size());
			HashMap<String, Food> fruit_map = toHashMap(fruit_list);
			ArrayList<String> fruit_prefer = preference.getFruit_list();
			buyFood("fruit", fruit_map, fruit_prefer, foodOrder);
			flags[2] = 1;
		}
		List<Food> dairy_list = stock.get("dairy");
		// System.out.println("dairy:"+dairy_list.size());
		if (dairy_list != null && flags[3] == 0) {
			// System.out.println("buy dairy: "+dairy_list.size());
			HashMap<String, Food> dairy_map = toHashMap(dairy_list);
			ArrayList<String> dairy_prefer = preference.getDairy_list();
			buyFood("dairy", dairy_map, dairy_prefer, foodOrder);
			flags[3] = 1;
		}

		List<Food> meat_list = stock.get("meat");
		// System.out.println("meat:"+meat_list.size());
		if (meat_list != null && flags[4] == 0) {
			// System.out.println("buy meat: "+meat_list.size());
			HashMap<String, Food> meat_map = toHashMap(meat_list);
			ArrayList<String> meat_prefer = preference.getMeat_list();
			buyFood("meat", meat_map, meat_prefer, foodOrder);
			flags[4] = 1;
		}

		this.buy_time++;
		this.avg_satisfaction += this.satisfaction;

		// System.out.println("finish selecting food, with order keys size:
		// "+foodOrder.getList().keySet().size());
		return foodOrder;
	}

	public HashMap<String, Food> toHashMap(List<Food> list) {
		HashMap<String, Food> foodHashMap = new HashMap<>();
		for (int i = 0; i < list.size(); i++) {
			Food f = list.get(i);
			foodHashMap.put(f.getName(), f);
		}
		return foodHashMap;

	}

	public boolean isEatingTime() {
		int hour = getHour();
		if ((hour >= 6 && hour <= 8) || (hour >= 11 && hour <= 13) || (hour >= 17 && hour <= 19)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isSleepingTime() {
		int hour = Helper.getCurrentTick();
		if (hour >= 21 || (hour >= 0 && hour <= 6)) {
			return true;
		} else {
			return false;
		}
	}

	public int getHour() {
		int tick = Helper.getCurrentTick();
		int day = tick / 144;
		int hour = (tick - day * 144) / 6;
		return hour;
	}

	public void buyFood(String s, HashMap<String, Food> foodMap, ArrayList<String> preference, FoodOrder foodOrder) {
		Random r = new Random();
		int reduce = 2;
		this.preference.set_final_weight();
		HashMap<String, Integer> final_weight = this.preference.get_final_weight();
		// System.out.println(s + " "+ final_weight+" buy food");
		// System.out.println("pre:"+preference.size());
		for (String name : preference) {
			Food f = foodMap.get(name);
			// System.out.println("name:"+ name);
			// if (f!=null) {
			// System.out.println("I want to buy "+f.getName() + f.getAmount());
			// }

			if (f != null && f.getAmount() > final_weight.get(s)/100) {
				foodOrder.addOrder(f, final_weight.get(s) / 100);
				// System.out.println("order:" +
				// f.getName()+final_weight.get(s)/100+foodOrder.getList().keySet().size());
			} else {
				this.satisfaction = this.satisfaction - reduce;
				reduce = reduce * 2;
			}
		}
	}

	public void consumeRandomFood() {
		Set<String> key = consumer_food_stock.keySet();
		Random r = new Random();
		for (String foodtype : key) {
			List<Food> list = consumer_food_stock.get(foodtype);
			int i = r.nextInt(list.size());
			Food f = list.get(i);
			list.remove(f);
			this.default_health += f.getAmount()*f.getDensity();
			this.caloryConsumption = f.getDensity()*100;
			f.setAmount(f.getAmount() - 100);
			list.add(f);
			consumer_food_stock.put(foodtype, list);
		}
	}

	public double getAvgSatisfaction() {
		return avg_satisfaction;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}