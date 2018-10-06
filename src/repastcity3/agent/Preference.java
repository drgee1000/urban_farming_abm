package repastcity3.agent;

import repastcity3.environment.food.FoodClassifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

public class Preference {
	//int organicity;
	//int freshness;
	//int price;
	ArrayList<String> grain_list;
	ArrayList<String> vegetable_list;
	ArrayList<String> fruit_list;
	ArrayList<String> meat_list;
	ArrayList<String> dairy_list;
	double d_weight = 0; // weight for distance;
	double s_weight = 0; // weight for score;
	
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
		Collections.shuffle(grain_list);
		Collections.shuffle(vegetable_list);
		Collections.shuffle(fruit_list);
		Collections.shuffle(meat_list);
		Collections.shuffle(dairy_list);
		Random r = new Random();
		this.d_weight = r.nextDouble();
		this.s_weight = 1 - this.d_weight;
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
