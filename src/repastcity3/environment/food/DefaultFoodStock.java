package repastcity3.environment.food;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import com.jidesoft.icons.IconSet.File;

import cern.jet.random.Normal;
import cern.jet.random.Uniform;
import gov.nasa.worldwind.data.RasterServerConfiguration.Source;
import gov.nasa.worldwind.formats.shapefile.ShapefilePolygons.Record;
import repast.simphony.random.*;
import repastcity3.exceptions.StockCreationException;;

public class DefaultFoodStock {
	
	public static ArrayList<Food> defaultFoodList;
	private static Uniform nRand ;
	public static String[] header;
	static {
		defaultFoodList=new ArrayList<>(50);
		nRand= RandomHelper.createUniform();
		try (Reader in = new FileReader("./data/food_data/food.csv")){
			CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader();
			Iterable<CSVRecord> records = format.parse(in);
			for (CSVRecord record : records) {
				//name,type,avg_price,carbohydrate,protein,lipid,water,vitamins,minerals
				//String name, String type, double amount,double price, double productionCost,Nutrition nutrition, double productionTime, double expireTime
				Food food=new Food(
						record.get("name"),
						record.get("type"),
						Double.valueOf(record.get("avg_price")),
						-1,
						Double.valueOf(record.get("avg_price")),
						-1,
						new Nutrition(
							Double.valueOf(record.get("carbohydrate")),
							Double.valueOf(record.get("protein")),
							Double.valueOf(record.get("lipid")),
							Double.valueOf(record.get("water")),
							Double.valueOf(record.get("vitamins")),
							Double.valueOf(record.get("minerals"))),
						-1,
						-1);
				defaultFoodList.add(food);
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public static List<Food> getRandomFoodList()
	{
		int n=RandomHelper.getUniform().nextIntFromTo(1, defaultFoodList.size());
		ArrayList<Food> foodList=new ArrayList<>();
		Collections.shuffle(defaultFoodList);
		
		for (int i= 0 ; i < n; i++) {
			Food srcFood=defaultFoodList.get(i);
			//todo: use more realistic 
			double amount,price,productionCost,productionTime,expireTime;
			double priceVar;
			amount=nRand.nextDoubleFromTo(0, 100);
			priceVar=nRand.nextDoubleFromTo(80,100);
			price=srcFood.getPrice()+srcFood.getPrice()/(nRand.nextDoubleFromTo(-1, 1)>0?priceVar:-priceVar);
			productionCost=srcFood.getPrice()/nRand.nextDoubleFromTo(0.5,0.98);
			productionTime=srcFood.getPrice()*nRand.nextDoubleFromTo(0.001,0.05);
			expireTime=productionTime*nRand.nextDoubleFromTo(1.3,10);
			Food destFood=new Food(
					srcFood.getName(),
					srcFood.getType(),
					srcFood.getCalorie(),
					amount,
					price,
					productionCost,
					srcFood.getNutrition(),
					productionTime,
					expireTime
					);
			foodList.add(destFood);
		}
		
		return foodList;
	}
	
	
	

	
	
}
