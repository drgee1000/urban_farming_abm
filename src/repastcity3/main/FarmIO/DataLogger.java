package repastcity3.main.FarmIO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import repast.simphony.util.collections.IndexedIterable;
import repastcity3.agent.Consumer;
import repastcity3.agent.IAgent;
import repastcity3.environment.Farm;
import repastcity3.environment.FixedGeography;
import repastcity3.environment.food.*;
import repastcity3.exceptions.NoIdentifierException;


public class DataLogger {
	char RecordSeparator;
	String fileName1;
	String fileNameFarm;
	String fileNameAgent;
	String fileName4;
	String fileName5;
	ArrayList<farm> farms = new ArrayList<>();
	ArrayList<agent> agents = new ArrayList<>();
	public DataLogger () throws IOException {
		// RecordSeparator = '\n';
		// fileName1 = "./Output/farm_calorie_production";
		fileNameFarm = "./output/Farm";
		fileNameAgent = "./output/agent";
		// fileName4 = "./Output/agent_calorie_consumption";
		// fileName5 = "./Output/agent_health";
		
		Calendar c= Calendar.getInstance();
		int year = c.get(Calendar.YEAR); 
		int month = c.get(Calendar.MONTH); 
		int date = c.get(Calendar.DATE); 
		int hour = c.get(Calendar.HOUR_OF_DAY); 
		int minute = c.get(Calendar.MINUTE); 
		int second = c.get(Calendar.SECOND); 
		String time = year + "_" + month + "_" + date + "_" +hour + "_" +minute + "_" + second; 
		// fileName1 = fileName1 + time + ".csv";
		fileNameFarm = fileNameFarm + time + ".json";
		fileNameAgent = fileNameAgent + time + ".json";
		// fileName4 = fileName4 + time + ".csv";
		// fileName5 = fileName5 + time + ".csv";
		// File f = new File(fileName1);
		// f.createNewFile();
		File f = new File(fileNameFarm);
		f.createNewFile();
		f = new File(fileNameAgent);
		f.createNewFile();
		// f = new File(fileName4);
		// f.createNewFile();
		// f = new File(fileName5);
		// f.createNewFile();
	}
	public DataLogger (char s) {
		RecordSeparator = s;
	}
	
	public <T> void recordData(IndexedIterable<T> agentList, int tick) throws IOException {
//		if(agentList.size()==0)
//			return;
		T t = agentList.get(0);
		if( t instanceof Farm) {
			Farm x = new Farm();
			farm f = new farm();
			for (int i = 0; i < agentList.size(); i++) {
				x = (Farm)(agentList.get(i));
				f = new farm(tick,x);
				farms.add(f);
			}
			
		} else if (t instanceof IAgent) {
			for (int i = 0; i < agentList.size(); i++) {
				Consumer x = (Consumer) agentList.get(i);
				agent a = new agent(tick, x); 
				agents.add(a);
			}
		}
		// if( t instanceof Farm) {
		// 	//System.out.println("exporting farm data");
			
		// 	ArrayList<String> list1 = new ArrayList<>();
		// 	ArrayList<String> list2 = new ArrayList<>();
		// 	ArrayList<String> list3 = new ArrayList<>();
			
		// 	String x;
		// 	for (int i = 0; i < agentList.size(); i++) {
		// 		x = agentList.get(i).toString();
		// 		list1.add((x));
		// 	}
			
		// 	printDataToCSVFile(list1, fileName1);
		// } else if (t instanceof IAgent) {
		// 	//System.out.println("exporting agent data");
			
		// 	//String fileName2 = "./Output/agent_waste.csv";
			
		// 	ArrayList<String> list1 = new ArrayList<>();
		// 	ArrayList<String> list2 = new ArrayList<>();
			
		// 	String x;
		// 	for (int i = 0; i < agentList.size(); i++) {
		// 		x = Double.toString(((DefaultAgent) agentList.get(i)).getCaloryConsumption());
		// 		list1.add((x));
		// 		x = Double.toString(((DefaultAgent) agentList.get(i)).getHealth());
		// 		list2.add(x);
		// 	}
			
		// 	printDataToCSVFile(list1, fileName4);
		// 	printDataToCSVFile(list2, fileName5);
		// }
		//System.out.println("start to export data to csv file");
	}
	
	// private void printDataToCSVFile(ArrayList<String> dataList, String fileName) throws IOException {
			
	// 		CSVFormat formator = CSVFormat.DEFAULT.withRecordSeparator(RecordSeparator);
	// 		Writer fileWriter = new FileWriter(fileName,true);
	// 		CSVPrinter printer = new CSVPrinter(fileWriter, formator);
			
	// 		printer.printRecord(dataList);
	// 		fileWriter.close();
	// 		printer.close();
	// 	}
	public void printDataToJsonFile() throws IOException {
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
		
		Writer fileWriter = new FileWriter(fileNameFarm);
		String jsonStr = gson.toJson(farms);
		fileWriter.write(jsonStr);
		
		fileWriter = new FileWriter(fileNameAgent);
		jsonStr = gson.toJson(agents);
		fileWriter.write(jsonStr);
		fileWriter.close();
	}
	
	
	// public <T> void printJsonData(IndexedIterable<T> agentList, int tick) throws IOException {
	// 	T t = agentList.get(0);
	// 	Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
		
	// }
	public static class farm {
		@Expose()
		String identifier;
		@Expose()
		int tick;
		@Expose()
		int stockNum;
		@Expose()
		List<Food> stock;
		@Expose()
		double count;
		public farm(int t, Farm f) {
			tick = t;
			stock = f.getStock();
			stockNum = stock.size();
			count=f.getCount();
			try {
				identifier = f.getIdentifier();
			} catch (NoIdentifierException e) {
				e.printStackTrace();
			}
			
		}
		public farm() {
			// TODO Auto-generated constructor stub
		}
	}
	public static class agent {
		@Expose()
		int tick;
		@Expose()
		double caloryConsumption;
		@Expose()
		double health;
		
		public agent(int t, Consumer a) {
			tick = t;
			caloryConsumption = a.getCaloryConsumption();
			health = a.getHealth();
		}
	}
	
}