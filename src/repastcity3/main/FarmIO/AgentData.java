package repastcity3.main.FarmIO;

class AgentOutputData {
	private double caloryConsumption;
	private double health;
	public AgentOutputData(double _caloryConsumption,double _health) {
		caloryConsumption = _caloryConsumption;
		health = _health;
	}
	public double getCaloryConsumption() {
		return caloryConsumption;
	}
	public double getHealth() {
		return health;
	}
}
