package repastcity3.environment.food;
import org.apache.poi.hssf.model.ConvertAnchor;

public class FoodConvertor {
	public static double health2nutrition(double health)
	{
		return health*2;
	}
	
	public static double nutrition2health(double nutrition)
	{
		return nutrition/2;
	}
	
	
}