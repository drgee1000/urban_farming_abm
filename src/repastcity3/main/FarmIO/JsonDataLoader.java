package repastcity3.main.FarmIO;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import repastcity3.environment.food.DefaultFoodStock;
import repastcity3.environment.food.Food;

public  class JsonDataLoader {
	String fileName;
	String[] headers;
	public static void main(String[] args) {
		System.out.println("hello");
		JsonDataLoader jdl = new JsonDataLoader();
		try {
			ArrayList<ProductionPlan> plans = jdl.load();
			for(ProductionPlan p : plans) {
				HashMap<String, Double> foods = p.getPlan();
				System.out.println(p.getTick());
				for(String name:foods.keySet()) {
					double amount = foods.get(name);
					Food destFood = DefaultFoodStock.getFoodByName(name, amount);
					System.out.println("name: "+ destFood.getName()+"amount:"+ destFood.getAmount());
					System.out.println("price: "+ destFood.getPrice()+"cost:" + destFood.getProductionCost()+" production time:"+ destFood.getProductionTime());
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public ArrayList<ProductionPlan> load() throws FileNotFoundException {
		fileName = "./data/farm/plan.json";
		BufferedReader buffReader = new BufferedReader(new FileReader(fileName));
		Gson gson = new Gson();
		ArrayList<Map> result = gson.fromJson(buffReader, ArrayList.class);
		ArrayList<ProductionPlan> productionPlans = new ArrayList<>();
		for (Map map:result) {
			int tick = 0;
			for (Object x : map.keySet()) {
				Object value = map.get(x);
				if(x.equals("tick")) {
					tick = ((Double)value).intValue();
				}
				if(x.equals("plan")) {
					HashMap<String,Double> pdPlan = new HashMap<String,Double>();
					ArrayList<Map> plans = gson.fromJson(value.toString(), ArrayList.class);
					for (Map plan : plans) {
						String name = "default";
						double amount = 0.00;
						for(Object i : plan.keySet()) {
							if(i.equals("name"))
								name = (String) plan.get(i);
							if(i.equals("amount")) {
								amount = (double)plan.get(i);
							}
						}
						pdPlan.put(name, amount);
					}
					productionPlans.add(new ProductionPlan(tick, pdPlan));
				}
			}
		}
		for (ProductionPlan pdp:productionPlans) {
			pdp.print();
		}
	return productionPlans;
	}
}
