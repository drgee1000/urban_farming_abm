package repastcity3.utilities.ioUtility;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import cern.jet.random.Uniform;
import repast.simphony.random.RandomHelper;
import repastcity3.environment.food.Food;

public class FoodUtility {

	private static ArrayList<Food> defaultFoodList;
	private static Uniform nRand;
	private static HashMap<String, List<Food>> allFood;
	private static HashMap<String, Food> foodData;
	public static String[] header;
	static {
		allFood = new HashMap<String, List<Food>>();
		foodData = new HashMap<String, Food>();
		defaultFoodList = new ArrayList<>(50);
		nRand = RandomHelper.getUniform();
		try (Reader in = new FileReader("./data/food_data/food.csv")) {
			CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader();
			Iterable<CSVRecord> records = format.parse(in);
			for (CSVRecord record : records) {
				Food food = new Food(record.get("name"), record.get("type"), Double.valueOf(record.get("calorie")), -1,
						Double.valueOf(record.get("avg_price")), Double.valueOf(record.get("cost")), -1, -1,
						Double.valueOf(record.get("value")), Double.valueOf(record.get("energyCost")));

				defaultFoodList.add(food);
				foodData.put(food.getName(), food);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		getRandomFoodList(1000, 2000);

	}

	public static Food getFoodByName(String name, double amount) {
		Food srcFood = foodData.get(name);
		double price, productionCost;
		int productionTime, expireTime;
		double priceVar;
		priceVar = nRand.nextDoubleFromTo(80, 100);
		price = srcFood.getPrice() + srcFood.getPrice() / (nRand.nextDoubleFromTo(-1, 1) > 0 ? priceVar : -priceVar);
		productionCost = srcFood.getPrice() * nRand.nextDoubleFromTo(0.5, 0.9);
		productionTime = (int) (nRand.nextDoubleFromTo(15, 60));
		expireTime = (int) (nRand.nextDoubleFromTo(1, 3));
		Food destFood = new Food(srcFood.getName(), srcFood.getType(), srcFood.getDensity(), amount, price,
				productionCost, productionTime, expireTime, srcFood.getValue(), srcFood.getEnergyCost());
		return destFood;
	}

	public static Food getFoodByType(String type) {
		List<Food> fList = allFood.get(type);
		int index = nRand.nextIntFromTo(0, fList.size() - 1);
		Food food = fList.get(index);
		return food;
	}

	public static List<Food> getSupermarketFoodList() {
		ArrayList<Food> foodList = new ArrayList<>();
		List<Food> fList = allFood.get("vegetable");
		System.out.println("get supermarket list, len:" + fList.size());
		Collections.shuffle(foodList);
		foodList.add(new Food(fList.get(0)));
		foodList.add(new Food(fList.get(1)));
		foodList.add(new Food(fList.get(2)));
		// System.out.println("return supermarket list, len:"+fL.size());
		return foodList;
	}

	public static List<Food> getLargeFarmFoodList() {
		List<Food> list = new ArrayList<>();
		for (Food f : allFood.get("vegetable")) {
			list.add(new Food(f));
		}
		return list;
	}

	public static List<Food> getRandomFoodList(int min, int max) {
		allFood = null;
		allFood = new HashMap<String, List<Food>>();
		ArrayList<String> foodTypes = new ArrayList<String>();
		foodTypes.add("vegetable");
		foodTypes.add("meat");
		foodTypes.add("diary");
		foodTypes.add("fruit");
		foodTypes.add("grain");
		int n = defaultFoodList.size();
		ArrayList<Food> foodList = new ArrayList<>();
		Collections.shuffle(defaultFoodList);

		for (int i = 0; i < n; i++) {
			Food srcFood = defaultFoodList.get(i);
			// todo: use more realistic
			double amount, price, productionCost;
			int productionTime, expireTime;
			double priceVar;
			amount = nRand.nextDoubleFromTo(min, max);
			priceVar = nRand.nextDoubleFromTo(80, 100);
			price = srcFood.getPrice();
			// + srcFood.getPrice() / (nRand.nextDoubleFromTo(-1, 1) > 0 ? priceVar :
			// -priceVar);
			productionCost = srcFood.getPrice() * nRand.nextDoubleFromTo(0.5, 0.9);
			productionTime = (int) (nRand.nextDoubleFromTo(15, 60));
			expireTime = (int) (nRand.nextDoubleFromTo(5, 10));
			Food destFood = new Food(srcFood.getName(), srcFood.getType(), srcFood.getDensity(), amount, price,
					productionCost, productionTime, expireTime, srcFood.getValue(), srcFood.getEnergyCost());
			foodList.add(destFood);
			String type = destFood.getType();

			if (allFood.containsKey(type)) {
				List<Food> fList = allFood.get(type);
				fList.add(destFood);
			} else {
				List<Food> fList = new ArrayList<Food>();
				fList.add(destFood);
				allFood.put(type, fList);
			}

		}

		return foodList;
	}

}