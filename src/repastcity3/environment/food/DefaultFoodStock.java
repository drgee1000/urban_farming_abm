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

import cern.jet.random.Uniform;
import gov.nasa.worldwind.formats.shapefile.ShapefilePolygons.Record;
import repast.simphony.random.*;;

public class DefaultFoodStock {
	
	public static ArrayList<Food> defaultFoodList;
	
	public static String[] header;
	static {
		defaultFoodList=new ArrayList<>(50);
		try (Reader in = new FileReader("./data/food_data/food.csv")){
			CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader();
			Iterable<CSVRecord> records = format.parse(in);
			for (CSVRecord record : records) {
				//name,type,avg_price,carbohydrate,protein,lipid,water,vitamins,minerals
				//String name, String type, double amount,double price, double productionCost,Nutrition nutrition, double productionTime, double expireTime
				Food food=new Food(
						record.get("name"),
						record.get("type"),
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
		int n=RandomHelper.getUniform().nextIntFromTo(0, defaultFoodList.size());
		ArrayList<Food> source=(ArrayList<Food>)defaultFoodList.clone();
		Collections.shuffle(source);
		for (int i= defaultFoodList.size() ; i >= n; i--) {
			source.remove(i-1);
		}
		
		return source;
	}
	
	
}
