package repastcity3.utilities.dataUtility;

import com.google.gson.annotations.SerializedName;

import repastcity3.agent.Catagory;
import repastcity3.agent.Income;

public class ConsumerType {

	public Catagory catagory;

	@SerializedName("mf_ratio")
	public double mfRatio;

	public double percentage;

	public Income income;

	public Double consumption_rate;

	public String food_preference;

	public String price_preference;

}
