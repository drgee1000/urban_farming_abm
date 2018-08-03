package repastcity3.main.FarmInput;

public class FarmInputData {
	private int area;
	private int plantsPerSqm;
	public FarmInputData() {
		area = 0;
		plantsPerSqm = 0;
		productionRate = 0;
	}
	public FarmInputData(String _area, String _plantsPerSqm, String _productionRate) {
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
