package repastcity3.main.FarmIO;

import java.util.HashMap;

public class ProductionPlan{
	public int tick;
	public HashMap<String, Double> plan;
	public ProductionPlan(int tick, HashMap<String, Double> plan) {
		this.tick = tick;
		this.plan = plan;
	}
	public void print() {
		System.out.println("tick:"+this.tick);
		for(String i:this.plan.keySet()) {
			System.out.println(i+":"+this.plan.get(i));
		}
	}
	public HashMap<String, Double> getPlan(){
		return plan;
	}
	public int getTick() {
		return tick;
	}
}