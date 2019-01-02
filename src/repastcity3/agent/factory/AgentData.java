package repastcity3.agent.factory;

import com.google.gson.annotations.SerializedName;

import repastcity3.agent.Catagory;
import repastcity3.agent.Income;

public class AgentData {

	public Catagory catagory;

	@SerializedName("mf_ratio")
	public double mfRatio;

	public double percentage;

	public Income income;

	public Double consumption_rate;

}
