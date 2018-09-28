package repastcity3.environment.food;

/*
 * this is the unit of every food or energy
 */
public class Nutrition {
	double carbohydrate;
	double protein;
	double lipid;//fat
	double water;
	double vitamins;
	double minerals;
	public Nutrition(double carbohydrate, double protein, double lipid, double water, double vitamins,
			double minerals) {
		super();
		this.carbohydrate = carbohydrate;
		this.protein = protein;
		this.lipid = lipid;
		this.water = water;
		this.vitamins = vitamins;
		this.minerals = minerals;
	}
	public double getCarbohydrate(){
		return carbohydrate;
	}
	public double getprotein(){
		return protein;
	}
	public double getlipid(){
		return lipid;
	}
	public double getwater(){
		return water;
	}
	public double getvitamins(){
		return vitamins;
	}
	public double getminerals(){
		return minerals;
	}
}
