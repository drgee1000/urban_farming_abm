package repastcity3.agent;

import com.google.gson.annotations.SerializedName;

public class AgentData {
	
	public Catagory catagory;
	
	@SerializedName("mf_ratio")
	public double mfRatio;
	
	public double percentage;
	
	public Income income;
	
	public Double consumption_rate;
	
}
