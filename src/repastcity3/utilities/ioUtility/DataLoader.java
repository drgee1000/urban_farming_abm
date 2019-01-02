package repastcity3.utilities.ioUtility;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import repastcity3.utilities.dataUtility.ProductionPlan;
import repastcity3.utilities.dataUtility.ProductionType;
import repastcity3.utilities.dataUtility.SupermarketType;
import repastcity3.environment.food.Food;

public  class DataLoader {
	static String fileName;
	String[] headers;
	public static void main(String[] args) {
		System.out.println("hello");
		DataLoader jdl = new DataLoader();
		try {
			ArrayList<ProductionPlan> plans = jdl.loadProductionPlan();
			for(ProductionPlan p : plans) {
				HashMap<String, Double> foods = p.getPlan();
				System.out.println(p.getTick());
				for(String name:foods.keySet()) {
					double amount = foods.get(name);
					Food destFood = FoodUtility.getFoodByName(name, amount);
					System.out.println("name: "+ destFood.getName()+"amount:"+ destFood.getAmount());
					System.out.println("price: "+ destFood.getPrice()+"cost:" + destFood.getProductionCost()+" production time:"+ destFood.getProductionTime());
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public ArrayList<ProductionPlan> loadProductionPlan() throws FileNotFoundException {
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
	public static ArrayList<ProductionType> loadProductionType(){
		ArrayList<ProductionType> typeList = new ArrayList<>();
		fileName = "./data/food_data/productionType.csv";
		try (Reader in = new FileReader(fileName)) {
			CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader();
			Iterable<CSVRecord> records = format.parse(in);
			for (CSVRecord record : records) {
				ProductionType pt = new ProductionType(Integer.valueOf(record.get("num")),
													   Double.valueOf(record.get("density")),
													   Integer.valueOf(record.get("period")),
													   Double.valueOf(record.get("price")),
													   Double.valueOf(record.get("tech")),
													   Double.valueOf(record.get("capacity")),
														Double.valueOf(record.get("priceFactor")));
				typeList.add(pt);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return typeList;
	}
	public static ArrayList<SupermarketType> loadSupermarketType(){
		ArrayList<SupermarketType> typeList = new ArrayList();
		fileName = "./data/agent_data/supermarketType.csv";
		try (Reader in = new FileReader(fileName)) {
			CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader();
			Iterable<CSVRecord> records = format.parse(in);
			for (CSVRecord record : records) {
				SupermarketType st = new SupermarketType(Double.valueOf(record.get("ratio")),
														Integer.valueOf(record.get("urbanPeriod")),
														Integer.valueOf(record.get("exPeriod")),
													   Double.valueOf(record.get("sourcingPlan")),
													   Double.valueOf(record.get("stockThreshold")),
													   Double.valueOf(record.get("radius")),
														Double.valueOf(record.get("priceFactor")));
				typeList.add(st);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return typeList;
	}
}
