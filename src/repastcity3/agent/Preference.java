package repastcity3.agent;

import repastcity3.environment.food.FoodClassifier;

import java.util.*;

public class Preference {
	//int organicity;
	//int freshness;
	//int price;
	ArrayList<String> grain_list;
	ArrayList<String> vegetable_list;
	ArrayList<String> fruit_list;
	ArrayList<String> meat_list;
	ArrayList<String> dairy_list;
	/*----------------------------*/
	HashMap<String, Double> grain_prefer;
	HashMap<String, Double> vegetable_prefer;
	HashMap<String, Double> fruit_prefer;
	HashMap<String, Double> meat_prefer;
	HashMap<String, Double> dairy_prefer;
	/*----------------------------*/
	double d_weight = 0; // weight for distance;
	double s_weight = 0; // weight for score;
	double personal = 0; // personal preference when selecting food
	double healthy = 0; // health consideration when selecting food
	HashMap<String, Integer> prefer_weight;
	HashMap<String, Integer> good_weight;
	HashMap<String, Integer> final_weight;

	//public int get_organicity() {
	//	return organicity;
	//}
	//public int get_freshness() {
	//	return freshness;
	//}
	//public int get_price() {
	//	return price;
	//}

	public Preference(){
		FoodClassifier foodClassifier = new FoodClassifier();
		grain_list = foodClassifier.getGrain_list();
		vegetable_list = foodClassifier.getVegetable_list();
		fruit_list = foodClassifier.getFruit_list();
		meat_list = foodClassifier.getMeat_list();
		dairy_list = foodClassifier.getDairy_list();
		Random r = new Random();
		grain_prefer = new HashMap<String, Double>();
		vegetable_prefer = new HashMap<String, Double>();
		fruit_prefer = new HashMap<String, Double>();
		meat_prefer = new HashMap<String, Double>();
		dairy_prefer = new HashMap<String, Double>();
		set_prefer_list(grain_list, grain_prefer);
		set_prefer_list(vegetable_list, vegetable_prefer);
		set_prefer_list(fruit_list, fruit_prefer);
		set_prefer_list(meat_list, meat_prefer);
		set_prefer_list(dairy_list, dairy_prefer);
		Collections.shuffle(grain_list);
		Collections.shuffle(vegetable_list);
		Collections.shuffle(fruit_list);
		Collections.shuffle(meat_list);
		Collections.shuffle(dairy_list);
		
		this.d_weight = r.nextDouble();
		this.s_weight = 1 - this.d_weight;
		this.prefer_weight = new HashMap<>();
		int weight = r.nextInt(300)+150;
		prefer_weight.put("grain", weight);
		weight = r.nextInt(400)+200;
		prefer_weight.put("vegetable", weight);
		weight = r.nextInt(300)+ 100;
		prefer_weight.put("fruit", weight);
		weight = r.nextInt(200) + 160;
		prefer_weight.put("meat", weight);
		weight = r.nextInt(400)+100;
		prefer_weight.put("dairy", weight);
		this.good_weight = new HashMap<>();
		weight = r.nextInt(200) + 250;
		good_weight.put("grain", weight);
		weight = r.nextInt(200)+300;
		good_weight.put("vegetable", weight);
		weight = r.nextInt(150)+ 200;
		good_weight.put("fruit", weight);
		weight = r.nextInt(80) + 160;
		good_weight.put("meat", weight);
		weight = 300;
		good_weight.put("dairy", weight);
		
		personal = r.nextDouble();
		healthy = 1 - personal;
		this.final_weight = new HashMap<>();
	}
	
	public void set_prefer_list(ArrayList<String> food_list, HashMap<String, Double> food_prefer) {
		Random r = new Random();
		double prefer_sum = 0;
		int rand_num = food_list.size();
		List<Double> rand_list = new ArrayList<Double>();
		for(int i=0; i<rand_num-1; i++) {
			rand_list.add(r.nextDouble());
		}
		Collections.sort(rand_list);
		// System.out.println(rand_list);
		int i = 0;
		
		for(i=0; i<rand_list.size(); i++) {
			double res = 0;
			if (i==0)
				res = rand_list.get(i);
			else
				res = rand_list.get(i) - rand_list.get(i-1);
			String food_name = food_list.get(i);
			food_prefer.put(food_name, res);
		}
		String food_name = food_list.get(i);
		if (rand_list.size() == 0)
			food_prefer.put(food_name, r.nextDouble());
		else
			food_prefer.put(food_name, 1-rand_list.get(i-1));
		// System.out.println(food_prefer);
		//System.out.println(rand_list);
		
	}
	public void set_final_weight(){
		final_weight.put("grain", get_final_weight_value("grain"));
		final_weight.put("vegetable", get_final_weight_value("vegetable"));
		final_weight.put("fruit", get_final_weight_value("fruit"));
		final_weight.put("meat", get_final_weight_value("meat"));
		final_weight.put("dairy", get_final_weight_value("dairy"));
	}
	
	public HashMap<String, Integer> get_final_weight(){
		return final_weight;
	}
	public int get_final_weight_value(String s){
		return (int) (personal*prefer_weight.get(s) + healthy*good_weight.get(s));
	}

	public double get_d_weight(){
		return d_weight;
	}

	public double get_s_weight(){
		return s_weight;
	}
	public ArrayList<String> getVegetable_list() {
		return vegetable_list;
	}

	public ArrayList<String> getMeat_list() {
		return meat_list;
	}

	public ArrayList<String> getGrain_list() {
		return grain_list;
	}

	public ArrayList<String> getFruit_list() {
		return fruit_list;
	}

	public ArrayList<String> getDairy_list() {
		return dairy_list;
	}
}
