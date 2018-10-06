package repastcity3.agent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import repastcity3.environment.Farm;
import repastcity3.environment.Residential;

import repastcity3.environment.Route;
import repastcity3.environment.School;
import repastcity3.environment.Supermarket;
import repastcity3.environment.Workplace;
import repastcity3.environment.food.Food;
import repastcity3.environment.food.FoodOrder;
import repastcity3.environment.food.FoodType;
import repastcity3.environment.food.Nutrition;
import repastcity3.main.ContextManager;
import repastcity3.utilities.Helper;

public class Consumer implements People {

	private static Logger LOGGER = Logger.getLogger(Consumer.class.getName());

	private Residential home; // Where the agent lives
	private Route route; // An object to move the agent around the world

	private Coordinate origin;
	private Coordinate destination;
	private Farm farm;
	private Residential residential;
	private int flag; // whether it has got enough food in one farm

	private boolean goforEat = false;
	private double defaultHealth = 200;
	private double health = 200;
	private double healthThreshold;
	private double caloryConsumption;
	private double caloryProduction;
	private double mealEnergy;
	private double deathThreshold;
	private int satisfaction;

	private static int uniqueID = 0;

	private double thredis = 2; // In kilometer;
	private int id;
	private int type = 3;

	private int income;
	private Map<String,Double> preference; 
	private HashMap<String, List<Food>> consumer_food_stock;
	
	private enum CATAGORY {
		CHILD, TEENAGER, ADULTS, OLD
	}
	private enum SEX{
		MALE, FEMALE
	}
	
	private CATAGORY catagory;
	private SEX sex;
	
	public Consumer() {
		this.id = uniqueID++;
		preference = new HashMap<String, Double>();
		consumer_food_stock = new HashMap<String, List<Food>>();
		Random random = new Random();
		this.residential = ContextManager.residentialContext.getRandomObject();
		int c = random.nextInt(100);
		if(c <= 7) {
			catagory = CATAGORY.CHILD;
			setSex();
			this.health = random.nextInt(50)+200;
			this.mealEnergy = random.nextInt(50)+300;
			setHealth(mealEnergy);
			File jsonFile = Paths.get("./data/agent_data/preference_child.json").toFile();
			setPreference(jsonFile);			
		}
		else if(c > 7 && c <= 17) {
			catagory = CATAGORY.TEENAGER;
			setSex();
			this.health = random.nextInt(100)+200;
			this.mealEnergy = random.nextInt(100)+300;
			setHealth(mealEnergy);
			File jsonFile = Paths.get("./data/agent_data/preference_teenager.json").toFile();
			setPreference(jsonFile);	
		}else if(c>17 && c<=97) {
			catagory = CATAGORY.ADULTS;
			setSex();
			this.health = random.nextInt(150)+200;
			this.mealEnergy = random.nextInt(150)+300;
			setHealth(mealEnergy);
			File jsonFile = Paths.get("./data/agent_data/preference_adults.json").toFile();
			setPreference(jsonFile);	
		}else {
			catagory = CATAGORY.OLD;
			setSex();
			this.health = random.nextInt(200)+200;
			this.mealEnergy = random.nextInt(200)+300;
			setHealth(mealEnergy);
			File jsonFile = Paths.get("./data/agent_data/preference_old.json").toFile();
			setPreference(jsonFile);	
		}
		
		setPurpose();
	}
	public void setSex() {
		Random random = new Random();
		int s = random.nextInt(100);
		if(s <= 44) 
			sex = SEX.FEMALE;
		else
			sex = SEX.MALE;
	}
	public void setHealth(int mealEnergy) {
		this.healthThreshold = 0.5*mealEnergy;
		this.deathThreshold = -9*mealEnergy;
		this.caloryConsumption = mealEnergy/50;
	}
	public void setPreference(File jsonFile) {

		/*Gson gson = new Gson();
		 try {
				JsonObject jsonObject = gson.fromJson(new FileReader(jsonFile), JsonObject.class);
				preference.put("carbohydrate", jsonObject.get("carbohydrate").getAsDouble());
				preference.put("protein", jsonObject.get("protein").getAsDouble());
				preference.put("lipid", jsonObject.get("lipid").getAsDouble());
				preference.put("water", jsonObject.get("water").getAsDouble());
				preference.put("vitamins", jsonObject.get("vitamins").getAsDouble());
				preference.put("minerals", jsonObject.get("minerals").getAsDouble());
			} catch (JsonSyntaxException|JsonIOException|FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
	}
	public int setPurpose() {
		Random random = new Random();
		this.type = random.nextInt(3)+1;
		return type;
	}
	
	
	@Override
	public void step() throws Exception {
		double stock_calory = getStockCalory(consumer_food_stock);
		if(stock_calory < 1000) {
			Farm farm = selectFarm();
			this.destination = ContextManager.farmProjection.getGeometry(farm).getCentroid().getCoordinate();
			GeometryFactory geomFac = new GeometryFactory();
			ContextManager.moveAgent(this, geomFac.createPoint(this.destination));
			FoodOrder foodOrder = this.selectFood(farm);
			farm.sell(foodOrder);
			farm.updateScore(this.satisfaction);
			}
		else {
			goRandomPlace(this.type);
			setPurpose();
		}
		this.health -= caloryConsumption;
		consumeRandomFood();
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

	
	
	public int getIncome() {
		return income;
	}

	public double getCaloryConsumption() {
		return caloryConsumption;
	}

	public double getCaloryProduction() {
		return caloryProduction;
	}

	public double getHealth() {
		return this.health;
	}

	public void setHealth(double health) {
		this.health = health;
	}

	public double getHealthThreshold() {
		return this.healthThreshold;
	}

	public void setHealthThreshold(double healthThreshold) {
		this.healthThreshold = healthThreshold;
	}

	public double getDefaultHealth() {
		return defaultHealth;
	}

	public void setDefaultHealth(double defaultHealth) {
		this.defaultHealth = defaultHealth;
	}
	

	public void goRandomPlace(int type) throws Exception {
		switch (type) {
		case 1: {
			School S = ContextManager.schoolContext.getRandomObject();
				//this.route = new Route(this,
				//		ContextManager.schoolProjection.getGeometry(S).getCentroid().getCoordinate(), S);
				//this.origin = ContextManager.getAgentGeometry(this).getCoordinate();
			this.destination = ContextManager.schoolProjection.getGeometry(S).getCentroid().getCoordinate();
			GeometryFactory geomFac = new GeometryFactory();
			ContextManager.moveAgent(this, geomFac.createPoint(this.destination));
			break;
		}
		case 2: {
			Workplace W = ContextManager.workplaceContext.getRandomObject();
				//this.route = new Route(this,
				//		ContextManager.workplaceProjection.getGeometry(W).getCentroid().getCoordinate(), W);
				//this.origin = ContextManager.getAgentGeometry(this).getCoordinate();
			this.destination = ContextManager.workplaceProjection.getGeometry(W).getCentroid().getCoordinate();
			GeometryFactory geomFac = new GeometryFactory();
			ContextManager.moveAgent(this, geomFac.createPoint(this.destination));
			break;
		}
		case 3: {
			this.destination = ContextManager.residentialProjection.getGeometry(this.residential).getCentroid().getCoordinate();
			GeometryFactory geomFac = new GeometryFactory();
			ContextManager.moveAgent(this, geomFac.createPoint(this.destination));
			break;
		}

		}
	}
	
	public Farm selectFarm() {
		Random random = new Random();
		Farm farm = null;
		int seed = random.nextInt(100);
		if(seed < 100) {
			farm = findNearestFarm();
		}else {
			farm = findPopularFarm();
		}
		return farm;
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
			this.origin = ContextManager.getAgentGeometry(this).getCoordinate();
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
	
	/*public Supermarket selectSupermarket() {
		
	}*/
	
	public double getSupermarketDistanceScore(Supermarket supermarket) {
		Iterator<Supermarket> iter = ContextManager.supermarketContext.iterator();
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		double distance = getDistance(supermarket);
		while(iter.hasNext()){
			Supermarket s = iter.next();
			double dis = getDistance(supermarket);
			if(dis<min){
				min = dis;
			}
			if(dis>max){
				max = dis;
			}
		}
		double score = (distance - min)/(max - min);
		return score;
	}

	public double getSupermarketRatingScore(Supermarket supermarket){
		Iterator<Supermarket> iter = ContextManager.supermarketContext.iterator();
		
		return 0;
	}
	public double getDistance(Supermarket supermarket){
		this.origin = ContextManager.getAgentGeometry(this).getCoordinate();
		this.destination = ContextManager.supermarketProjection.getGeometry(supermarket).getCentroid().getCoordinate();
		double dis = (origin.x - destination.x) * (origin.x - destination.x)
				+ (origin.y - destination.y) * (origin.y - destination.y);
		return dis;
	}
	public Supermarket findNearestSupermarket() {
		Iterator<Supermarket> iter = ContextManager.supermarketContext.iterator();
		double min = Double.POSITIVE_INFINITY;
		Supermarket nearestSupermarket = null;
		// may not iterate all the farms.
		int iterTime = 100;
		while (iter.hasNext()) {
			Supermarket supermarket = iter.next();
			Route r = new Route(this, ContextManager.supermarketProjection.getGeometry(supermarket).getCentroid().getCoordinate(),
					supermarket);
			this.origin = ContextManager.getAgentGeometry(this).getCoordinate();
			this.destination = ContextManager.supermarketProjection.getGeometry(supermarket).getCentroid().getCoordinate();
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
		while(iter.hasNext()) {
			Farm farm = iter.next();
			double score = farm.getScore();
			if(score > max) {
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
		while(iter.hasNext()) {
			Supermarket supermarket = iter.next();
			double score = supermarket.getScore();
			if(score > max) {
				max = score;
				PopularSupermarket = supermarket;		
			}
		}
		return PopularSupermarket;
	}
	double getFoodScore(Food food) {
		Nutrition ntr = food.getNutrition();
		// to-do: normalize the value of each kind of nutrition (divide the max-min value )
		double score = preference.get("lipid")*ntr.getlipid()+preference.get("carbohydrate")*ntr.getCarbohydrate()+preference.get("protein")*ntr.getprotein()+preference.get("water")*ntr.getwater()+preference.get("vitamins")*ntr.getvitamins()+preference.get("minerals")*ntr.getminerals();
		return score;
	}
	double getStockCalory(HashMap<String, List<Food>> stock) {
		double calory_sum = 0;
		Set<String> key = stock.keySet();
		for(String foodType:key) {
			List<Food> list = stock.get(foodType);
			for(Food f: list){
				calory_sum += f.getCalorie()*f.getAmount();
			}
		}
		return calory_sum;
	}
	
	public FoodOrder selectFood(Farm farm) {
			//System.err.println("enter select food");

			FoodOrder foodOrder = new FoodOrder();
			this.satisfaction = 100;
			HashMap<String, List<Food>> stock = farm.getStock();
			Preference preference = new Preference();
			List<Food> grain_list = stock.get("grain");
			if(grain_list != null) {
				System.out.println("grain: "+grain_list.size());
				HashMap<String, Food> grain_map = toHashMap(grain_list);
				ArrayList<String> grain_prefer = preference.getGrain_list();
				buyFood(grain_map,grain_prefer,foodOrder);
			}
			List<Food> vegetable_list = stock.get("vegetable");
			if(vegetable_list != null){
				System.out.println("vegetable: "+vegetable_list.size());
				HashMap<String, Food> vegetable_map = toHashMap(vegetable_list);
				ArrayList<String> vegetable_prefer = preference.getVegetable_list();
				buyFood(vegetable_map,vegetable_prefer,foodOrder);
			}
			List<Food> fruit_list = stock.get("fruit");
			if(fruit_list!=null){
				System.out.println("fruit: "+fruit_list.size());
				HashMap<String, Food> fruit_map = toHashMap(fruit_list);
				ArrayList<String> fruit_prefer = preference.getFruit_list();
				buyFood(fruit_map,fruit_prefer,foodOrder);
			}
			List<Food> dairy_list = stock.get("dairy");
			if(dairy_list != null){
				System.out.println("dairy: "+dairy_list.size());
				HashMap<String, Food> dairy_map = toHashMap(dairy_list);
				ArrayList<String> dairy_prefer = preference.getDairy_list();
				buyFood(dairy_map,dairy_prefer,foodOrder);
			}

			List<Food> meat_list = stock.get("meat");
			if(meat_list != null){
				System.out.println("meat: "+meat_list.size());
				HashMap<String, Food> meat_map = toHashMap(meat_list);
				ArrayList<String> meat_prefer = preference.getMeat_list();
				buyFood(meat_map,meat_prefer,foodOrder);
			}
			return foodOrder;
	}

	public HashMap<String, Food> toHashMap(List<Food> list){
		HashMap<String, Food> foodHashMap = new HashMap<>();
		for(Food f: list){
			foodHashMap.put(f.getName(),f);
		}
		return foodHashMap;
	}
	public boolean isEatingTime() {
		int hour = getHour();
		if((hour>=6 && hour<=8) || (hour>=11 && hour<=13) || (hour>=17 && hour<=19)) {
			return true;
		}else {
			return false;
		}
	}
	public boolean isSleepingTime() {
		int hour = Helper.getCurrentTick();
		if(hour >= 21 || (hour>=0 && hour<=6)) {
			return true;
		}else {
			return false;
		}
	}
	public int getHour() {
		int tick = Helper.getCurrentTick();
		int day = tick/144;
		int hour = (tick-day*144)/6;
		return hour;
	}
	public void buyFood(HashMap<String, Food> foodMap, ArrayList<String> preference, FoodOrder foodOrder){
		Random r = new Random();
		for(String name: preference){
			Food f = foodMap.get(name);
			if (f!=null && f.getAmount() > 0){
				foodOrder.addOrder(f, r.nextDouble()*5);
			}else{
				this.satisfaction--;
			}
		}
	}
	public void consumeRandomFood(){
		Set<String> key = consumer_food_stock.keySet();
		Random r = new Random();
		for(String foodtype: key){
			List<Food> list = consumer_food_stock.get(foodtype);
			int i = r.nextInt(list.size());
			Food f = list.get(i);
			list.remove(f);
			f.setAmount(f.getAmount()-1);
			list.add(f);
			consumer_food_stock.put(foodtype, list);
		}
	}
}