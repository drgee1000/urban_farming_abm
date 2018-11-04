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
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.hsqldb.lib.Iterator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import repast.simphony.util.collections.IndexedIterable;
import repastcity3.agent.Consumer;
import repastcity3.agent.IAgent;
import repastcity3.agent.People;
import repastcity3.environment.Farm;
import repastcity3.environment.FixedGeography;
import repastcity3.environment.Supermarket;
import repastcity3.environment.food.*;
import repastcity3.exceptions.NoIdentifierException;

public class DataLogger {
	String fileNameFarm;
	String fileNameAgent;
	String fileNameDeathRecord;
	String fileNameSales;
	String fileNameSupermarket;

	Gson gson;
	Writer agentFileWriter;
	Writer farmFileWriter;
	Writer drFileWriter;
	Writer salesFileWriter;
	Writer supermarketFileWriter;
	ArrayList<deathRecord> dRecords;
	public DataLogger() throws IOException {
		fileNameFarm = "./output/Farm";
		fileNameAgent = "./output/Agent";
		fileNameDeathRecord = "./output/Death";
		fileNameSales = "./output/Sales";
		fileNameSupermarket = "./output/Supermarket";
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int date = c.get(Calendar.DATE);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int second = c.get(Calendar.SECOND);
		String time = year + "_" + month + "_" + date + "_" + hour + "_" + minute + "_" + second;
		fileNameFarm = fileNameFarm + time + ".json";
		fileNameAgent = fileNameAgent + time + ".json";
		fileNameDeathRecord = fileNameDeathRecord + time + ".json";
		fileNameSales = fileNameSales + time + ".json";
		fileNameSupermarket = fileNameSupermarket+time+".json";
		// f.createNewFile();
		File f = new File(fileNameFarm);
		f.createNewFile();
		f = new File(fileNameAgent);
		f.createNewFile();
		f = new File(fileNameDeathRecord);
		f.createNewFile();
		f = new File(fileNameSales);
		f.createNewFile();
		f = new File(fileNameSupermarket);
		f.createNewFile();
		
		gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
		agentFileWriter = new FileWriter(fileNameAgent);
		agentFileWriter.write('[');
		farmFileWriter = new FileWriter(fileNameFarm);
		farmFileWriter.write('[');
		salesFileWriter = new FileWriter(fileNameSales);
		salesFileWriter.write('[');
		supermarketFileWriter = new FileWriter(fileNameSupermarket);
		supermarketFileWriter.write('[');
		drFileWriter = new FileWriter(fileNameDeathRecord);
		dRecords =  new ArrayList<deathRecord>();
	}

	public <T> void recordData(IndexedIterable<T> agentList, int tick) throws IOException {
		if (agentList.size() == 0)
			return;

		ArrayList<farm> farms = new ArrayList<>();
		ArrayList<agent> agents = new ArrayList<>();
		ArrayList<supermarket> supermarkets = new ArrayList<>();
		T t = agentList.get(0);
		if (t instanceof Farm) {
			Farm x = new Farm();
			farm f = new farm();
			for (int i = 0; i < agentList.size(); i++) {
				x = (Farm) (agentList.get(i));
				f = new farm(tick, x);
				farms.add(f);
			}

		} else if (t instanceof People) {
			for (int i = 0; i < agentList.size(); i++) {
				Consumer x = (Consumer) agentList.get(i);
				agent a = new agent(tick, x);
				agents.add(a);
			}
		} else if(t instanceof Supermarket) {
			
			for(int i = 0; i < agentList.size();i++) {
				Supermarket S = (Supermarket)agentList.get(i);
				supermarket s = new supermarket(tick,S);
				supermarkets.add(s);
			}	
			
		}

		// printDataToJsonFile
		String farmJson = gson.toJson(farms);
		farmFileWriter.write(farmJson);
		farmFileWriter.write(',');
		
		String supermarketJson = gson.toJson(supermarkets);
		supermarketFileWriter.write(supermarketJson);
		supermarketFileWriter.write(',');
		String agentJson = gson.toJson(agents);
		agentFileWriter.write(agentJson);
		agentFileWriter.write(',');
	}
	
	public void recordDeath(int t, int i) {
		deathRecord dr = new deathRecord(t,i);
		dRecords.add(dr);
	}
	public void recordSale (FoodOrder foodOrder, int tick, double income, String id,String consumerID) throws IOException{
		saleRecord sr = new saleRecord(foodOrder, income, tick, id,consumerID);
		// System.out.println(id+" sell amount: "+foodOrder.getList().size());
		//System.out.println("farm" + sr.identifier + " sale income:" + sr.income + " tick: " + tick);
		String salesJson = gson.toJson(sr);
		salesFileWriter.write(salesJson);
		salesFileWriter.write(',');
	}
	public void stopRecord() throws IOException {
		System.out.println("total:" + dRecords.size());
		String drJson = gson.toJson(dRecords);
		drFileWriter.write(drJson);
		agentFileWriter.write("[]]");
		agentFileWriter.close();
		farmFileWriter.write("[]]");
		farmFileWriter.close();
		supermarketFileWriter.write("[]]");
		supermarketFileWriter.close();
		drFileWriter.close();
		salesFileWriter.write("[]]");
		salesFileWriter.close();
	}

	public static class farm {
		@Expose()
		String identifier;
		@Expose()
		int tick;
		@Expose()
		int stockNum;
		//@Expose()
		//HashMap<String,List<Food>> stock;
		//@Expose()
		//double count;
		@Expose()
		double fund;
		@Expose()
		HashMap<String,List<FoodEntry>> waste;
		public farm(int t, Farm f) {
			waste = f.getWaste();
			tick = t;
			//stock = f.getStock();
			stockNum = f.getStock().size();
			//count = f.getCount();
			fund = f.getFund();
			try {
				identifier = f.getIdentifier();
			} catch (NoIdentifierException e) {
				e.printStackTrace();
			}

		}
		public farm() {}
	}

	public static class agent {
		@Expose()
		String id;
		@Expose()
		int tick;
		@Expose()
		double caloryConsumption;
		@Expose()
		double health;
		@Expose()
		double satisfaction;
		

		public agent(int t, Consumer a) {
			satisfaction = a.getAvgSatisfaction();
			id = a.toString();
			tick = t;
			caloryConsumption = a.getCaloryConsumption();
			health = a.getHealth();
		}
	}
	public static class deathRecord {
		@Expose()
		int tick;
		@Expose()
		int id;
		public deathRecord(int t, int i) {
			tick = t;
			id = i;
		}
	}
	public static class saleRecord {
		
		@Expose()
		double income;
		@Expose()
		int tick;
		@Expose()
		String identifier;
		@Expose()
		String consumerID;
		@Expose()
		HashMap<String, Double> order;
		public saleRecord (FoodOrder foodOrder, double i, int t, String id,String cID){
			identifier = id;
			consumerID = cID;
			income = i;
			tick = t;
			Set<Food> fSet = foodOrder.getList().keySet();
			order = new HashMap<String, Double>();
			java.util.Iterator<Food> iter = fSet.iterator();
			while(iter.hasNext()) {
				Food f = (Food) iter.next();
				Double val = foodOrder.getList().get(f);
				order.put(f.getName(), val);
			}
		}
	}
	public static class supermarket {
		@Expose()
		String identifier;
		@Expose()
		int tick;
		@Expose()
		int stockNum;
		//@Expose()
		//HashMap<String,List<Food>> stock;
		//@Expose()
		//double count;
		@Expose()
		double fund;
		@Expose()
		HashMap<String,List<FoodEntry>> waste;

		public supermarket(int t, Supermarket s) {
			waste = s.getWaste();
			this.tick = t;
			//stock = s.getStock();
			stockNum = s.getStock().keySet().size();
			//count = f.getCount();
			fund = s.getFund();
			try {
				identifier = s.getIdentifier();
			} catch (NoIdentifierException e) {
				e.printStackTrace();
			}

		}
		public supermarket() {}
		
	}
}