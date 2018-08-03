package repastcity3.main.FarmInput;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import gov.nasa.worldwind.formats.shapefile.ShapefilePolygons.Record;
import repast.simphony.util.collections.IndexedIterable;
import repastcity3.agent.DefaultAgent;
import repastcity3.agent.IAgent;
import repastcity3.environment.Farm;
import repastcity3.environment.FixedGeography;

public class DataLoader {
	public DataLoader(String fileName, int NumAgents) {
		String[] FILE_HEADER = {"Area","plants per sqm","production rate"};
		String Area,PlantsPerSqm,ProductionRate;
		CSVFormat format = CSVFormat.DEFAULT.withHeader(FILE_HEADER).withSkipHeaderRecord();
		List<FarmInputData> farmData = null;
		try {
			Reader in = new FileReader(fileName);
			Iterable<CSVRecord> records = format.parse(in);
			for (CSVRecord record : records) {
				Area = record.get("Area");
				PlantsPerSqm = record.get("Plants per sqm");
				ProductionRate= record.get("production rate");
				FarmInputData fData = new FarmInputData(Area, PlantsPerSqm,ProductionRate);
				farmData.add(fData);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
