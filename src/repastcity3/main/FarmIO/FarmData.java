package repastcity3.main.FarmIO;

class FarmData {
	private int area;
	private int plantsPerSqm;
	private int productionRate;
	public FarmData() {
		area = 0;
		plantsPerSqm = 0;
		productionRate = 0;
	}
	public FarmData(String _area, String _plantsPerSqm, String _productionRate) {
		try{
			area = Integer.parseInt(_area);
			plantsPerSqm = Integer.parseInt(_plantsPerSqm);
			productionRate = Integer.parseInt(_productionRate);
		} catch(Exception e) {
			e.printStackTrace();
		}	
	}
	public int getArea() {
		return area;
	}
	public int getPlantsPerSqm() {
		return plantsPerSqm;
	}
	public int getProductionRate() {
		return productionRate;
	}
}
