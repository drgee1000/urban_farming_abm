package repastcity3.utilities.ioUtility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import repastcity3.environment.food.Food;
import repastcity3.utilities.dataUtility.ProductionPlan;
import repastcity3.utilities.dataUtility.AgentTypeGenerator;
import repastcity3.utilities.dataUtility.ConsumerType;
import repastcity3.utilities.dataUtility.FarmType;
import repastcity3.utilities.dataUtility.SupermarketType;

public class DataLoader {

//	public static void main(String[] args) {
//		System.out.println("hello");
//		DataLoader jdl = new DataLoader();
//		try {
//			ArrayList<ProductionPlan> plans = jdl.loadProductionPlan();
//			for (ProductionPlan p : plans) {
//				HashMap<String, Double> foods = p.getPlan();
//				System.out.println(p.getTick());
//				for (String name : foods.keySet()) {
//					double amount = foods.get(name);
//					Food destFood = FoodUtility.getFoodByName(name, amount);
//					System.out.println("name: " + destFood.getName() + "amount:" + destFood.getAmount());
//					System.out.println("price: " + destFood.getPrice() + "cost:" + destFood.getProductionCost()
//							+ " production time:" + destFood.getProductionTime());
//				}
//			}
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	public static ArrayList<ProductionPlan> loadProductionPlan() throws FileNotFoundException {
		String fileName = "./data/farm/plan.json";
		BufferedReader buffReader = new BufferedReader(new FileReader(fileName));
		Gson gson = new Gson();
		ArrayList<Map> result = gson.fromJson(buffReader, ArrayList.class);
		ArrayList<ProductionPlan> productionPlans = new ArrayList<>();
		for (Map map : result) {
			int tick = 0;
			for (Object x : map.keySet()) {
				Object value = map.get(x);
				if (x.equals("tick")) {
					tick = ((Double) value).intValue();
				}
				if (x.equals("plan")) {
					HashMap<String, Double> pdPlan = new HashMap<String, Double>();
					ArrayList<Map> plans = gson.fromJson(value.toString(), ArrayList.class);
					for (Map plan : plans) {
						String name = "default";
						double amount = 0.00;
						for (Object i : plan.keySet()) {
							if (i.equals("name"))
								name = (String) plan.get(i);
							if (i.equals("amount")) {
								amount = (double) plan.get(i);
							}
						}
						pdPlan.put(name, amount);
					}
					productionPlans.add(new ProductionPlan(tick, pdPlan));
				}
			}
		}
		for (ProductionPlan pdp : productionPlans) {
			pdp.print();
		}
		return productionPlans;
	}

	public static ArrayList<ConsumerType> loadConsumerType(String filename) throws IOException {

		// read agent config file
		File file = new File(filename);
		FileReader fileReader = new FileReader(file);
		BufferedReader bReader = new BufferedReader(fileReader);
		Gson gson = new Gson();
		String data = bReader.readLine();
		StringBuffer sb = new StringBuffer();
		while (data != null) {
			sb.append(data);
			data = bReader.readLine();
		}
		ArrayList<ConsumerType> agentDatas = gson.fromJson(sb.toString(), new TypeToken<List<ConsumerType>>() {
		}.getType());
		int agentTypeSize = agentDatas.size();
		System.out.println("agentTypeSize: " + agentTypeSize);
		return agentDatas;

	}

	public static ArrayList<FarmType> loadFarmType(String fileName) {
		ArrayList<FarmType> typeList = new ArrayList<>();
		try (Reader in = new FileReader(fileName)) {
			CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader();
			Iterable<CSVRecord> records = format.parse(in);
			for (CSVRecord record : records) {
				FarmType pt = new FarmType(Integer.valueOf(record.get("type")),Double.valueOf(record.get("percentage")),
						Double.valueOf(record.get("density")), Integer.valueOf(record.get("period")),
						Double.valueOf(record.get("price")), Double.valueOf(record.get("tech")),
						Double.valueOf(record.get("capacity")), Double.valueOf(record.get("priceFactor")),
						Double.valueOf(record.get("area")));
				typeList.add(pt);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return typeList;
	}

	public static ArrayList<SupermarketType> loadSupermarketType(String fileName) {
		ArrayList<SupermarketType> typeList = new ArrayList();
		try (Reader in = new FileReader(fileName)) {
			CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader();
			Iterable<CSVRecord> records = format.parse(in);
			for (CSVRecord record : records) {
				SupermarketType st = new SupermarketType(Double.valueOf(record.get("percentage")),
						Integer.valueOf(record.get("urbanPeriod")), Integer.valueOf(record.get("exPeriod")),
						Double.valueOf(record.get("sourcingPlan")), Double.valueOf(record.get("stockThreshold")),
						Double.valueOf(record.get("radius")), Double.valueOf(record.get("priceFactor")));
				typeList.add(st);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return typeList;
	}
}
