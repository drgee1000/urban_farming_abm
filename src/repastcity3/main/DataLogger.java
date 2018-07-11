package repastcity3.main;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import repast.simphony.util.collections.IndexedIterable;
import repastcity3.agent.DefaultAgent;
import repastcity3.agent.IAgent;
import repastcity3.environment.Farm;
import repastcity3.environment.FixedGeography;

public class DataLogger {
	char RecordSeparator;
	public DataLogger () {
		RecordSeparator = '\n';
	}
	public DataLogger (char s) {
		RecordSeparator = s;
	}
	
	public <T> void printData(IndexedIterable<T> agentList) throws IOException {
		T t = agentList.get(0);
		if( t instanceof Farm) {
			//System.out.println("exporting farm data");
			String fileName1 = "./Output/farm_calorie_production.csv";
			String fileName2 = "./Output/farm_organic_waste.csv";
			String fileName3 = "./Output/agent_waste.csv";
			ArrayList<String> list1 = new ArrayList<>();
			ArrayList<String> list2 = new ArrayList<>();
			ArrayList<String> list3 = new ArrayList<>();
			
			String x;
			for (int i = 0; i < agentList.size(); i++) {
				x = agentList.get(i).toString();
				list1.add((x));
			}
			
			printDataToFile(list1, fileName1);
		} else if (t instanceof IAgent) {
			//System.out.println("exporting agent data");
			String fileName1 = "./Output/agent_calorie_consumption.csv";
			String fileName2 = "./Output/agent_waste.csv";
			ArrayList<String> list1 = new ArrayList<>();
			ArrayList<String> list2 = new ArrayList<>();
			
			String x;
			for (int i = 0; i < agentList.size(); i++) {
				x = Double.toString(((DefaultAgent) agentList.get(i)).getCaloryConsumption());
				list1.add((x));
			}
			
			printDataToFile(list1, fileName1);
		}
		//System.out.println("start to export data to csv file");
	}
	
	public void printDataToFile(ArrayList<String> dataList, String fileName) throws IOException {
		CSVFormat formator = CSVFormat.DEFAULT.withRecordSeparator(RecordSeparator);
		Writer fileWriter = new FileWriter(fileName,true);
		CSVPrinter printer = new CSVPrinter(fileWriter, formator);
		
		printer.printRecord(dataList);
		fileWriter.close();
		printer.close();
	}
}