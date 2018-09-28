package repastcity3.main.FarmIO;

class FarmInputData {
	private int area;
	private int plantsPerSqm;
	private int productionRate;
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
class FarmOutputData{
	private int stockType;
	
	public FarmOutputData() {
	
	}
	public FarmOutputData(int _stockType) {
		try{
			stockType = _stockType;
		} catch(Exception e) {
			e.printStackTrace();
		}	
	}
	public int getStockType() {
		return stockType;
	}
}
