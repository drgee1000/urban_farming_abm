package repastcity3.main.FarmIO;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import repast.simphony.util.collections.IndexedIterable;
import repastcity3.agent.DefaultAgent;
import repastcity3.agent.IAgent;
import repastcity3.environment.Farm;
import repastcity3.environment.FixedGeography;
import repastcity3.environment.food.*;


public class DataLogger {
	char RecordSeparator;
	String fileName1;
	String fileName2;
	String fileName3;
	String fileName4;
	String fileName5;
	public DataLogger () throws IOException {
		RecordSeparator = '\n';
		fileName1 = "./Output/farm_calorie_production";
		fileName2 = "./Output/farm_organic_waste";
		fileName3 = "./Output/agent_waste";
		fileName4 = "./Output/agent_calorie_consumption.csv";
		fileName5 = "./Output/agent_health.csv";
		Calendar c= Calendar.getInstance();
		int year = c.get(Calendar.YEAR); 
		int month = c.get(Calendar.MONTH); 
		int date = c.get(Calendar.DATE); 
		int hour = c.get(Calendar.HOUR_OF_DAY); 
		int minute = c.get(Calendar.MINUTE); 
		int second = c.get(Calendar.SECOND); 
		String time = year + "_" + month + "_" + date + "_" +hour + "_" +minute + "_" + second; 
		fileName1 = fileName1 + time + ".csv";
		fileName2 = fileName2 + time + ".csv";
		fileName3 = fileName3 + time + ".csv";
		fileName4 = fileName4 + time + ".csv";
		fileName5 = fileName5 + time + ".csv";
		File f = new File(fileName1);
		f.createNewFile();
		f = new File(fileName2);
		f.createNewFile();
		f = new File(fileName3);
		f.createNewFile();
		f = new File(fileName4);
		f.createNewFile();
		f = new File(fileName5);
		f.createNewFile();
	}
	public DataLogger (char s) {
		RecordSeparator = s;
	}
	
	public <T> void printData(IndexedIterable<T> agentList, int tick) throws IOException {
		T t = agentList.get(0);
		printJsonData(agentList,tick);
		if( t instanceof Farm) {
			//System.out.println("exporting farm data");
			
			ArrayList<String> list1 = new ArrayList<>();
			ArrayList<String> list2 = new ArrayList<>();
			ArrayList<String> list3 = new ArrayList<>();
			
			String x;
			for (int i = 0; i < agentList.size(); i++) {
				x = agentList.get(i).toString();
				list1.add((x));
			}
			
			printDataToCSVFile(list1, fileName1);
		} else if (t instanceof IAgent) {
			//System.out.println("exporting agent data");
			
			//String fileName2 = "./Output/agent_waste.csv";
			
			ArrayList<String> list1 = new ArrayList<>();
			ArrayList<String> list2 = new ArrayList<>();
			
			String x;
			for (int i = 0; i < agentList.size(); i++) {
				x = Double.toString(((DefaultAgent) agentList.get(i)).getCaloryConsumption());
				list1.add((x));
				x = Double.toString(((DefaultAgent) agentList.get(i)).getHealth());
				list2.add(x);
			}
			
			printDataToCSVFile(list1, fileName4);
			printDataToCSVFile(list2, fileName5);
		}
		//System.out.println("start to export data to csv file");
	}
	
	public void printDataToCSVFile(ArrayList<String> dataList, String fileName) throws IOException {
		
		CSVFormat formator = CSVFormat.DEFAULT.withRecordSeparator(RecordSeparator);
		Writer fileWriter = new FileWriter(fileName,true);
		CSVPrinter printer = new CSVPrinter(fileWriter, formator);
		
		printer.printRecord(dataList);
		fileWriter.close();
		printer.close();
	}
	
	public void printDataToJsonFile() {
		
	}
	public <T> void printJsonData(IndexedIterable<T> agentList, int tick) throws IOException {
		T t = agentList.get(0);
		Gson gson = new Gson();
		if( t instanceof Farm) {
			for (int i = 0; i < agentList.size(); i++) {
				Farm x = (Farm)(agentList.get(i));
				farm f = new farm(tick,x.getStock());
				
				String jsonStr = gson.toJson(f);
				System.out.print(jsonStr);
			}
			
		} else if (t instanceof IAgent) {
			for (int i = 0; i < agentList.size(); i++) {
				DefaultAgent x = (DefaultAgent) agentList.get(i);
				agent a = new agent(); 
				a.setTick(tick);
				a.setCaloryConsumption(x.getCaloryConsumption());
				a.setHealth(x.getHealth());
				String jsonStr = gson.toJson(a);
				System.out.print(jsonStr);
				
				
			}
		System.out.print("call json");
		}
	}
	public static class farm {
		int tick;
		int stockNum;
		List<Food> stock;
		public farm(int t, List<Food> s) {
			tick = t;
			stock = s;
			stockNum = s.size();
		}
	}
	public static class agent {
		int tick;
		double caloryConsumption;
		double health;
		public void setTick(int t) {
			tick = t;
		}
		public void setCaloryConsumption(double c) {
			caloryConsumption = c;
		}
		public void setHealth(double h) {
			health = h;
		}
	}
	
}