package repastcity3.utilities.ioUtility;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import repast.simphony.util.collections.IndexedIterable;
import repastcity3.agent.Consumer;
import repastcity3.agent.Farm;
import repastcity3.agent.IAgent;
import repastcity3.agent.Supermarket;
import repastcity3.environment.SaleLocation;
import repastcity3.environment.food.Food;
import repastcity3.environment.food.FoodOrder;
import repastcity3.environment.food.Waste;
import repastcity3.exceptions.NoIdentifierException;
import repastcity3.main.AgentControl;

public class DataLogger {
	String fileNameFarm;
	String fileNameAgent;
	String fileNameEnding;
	String fileNameSales;
	String fileNameSupermarket;

	Gson gson;
	Writer agentFileWriter;
	Writer farmFileWriter;
	Writer endingFileWriter;
	Writer salesFileWriter;
	Writer supermarketFileWriter;
	int salesFileCount;
	String time;

	public DataLogger() throws IOException {
		salesFileCount = 1;
		fileNameFarm = "./output/Farm";
		fileNameAgent = "./output/Agent";
		fileNameEnding = "./output/End";
		fileNameSales = "./output/Sales";
		fileNameSupermarket = "./output/Supermarket";
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		int date = c.get(Calendar.DATE);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int second = c.get(Calendar.SECOND);
		time = year + "_" + month + "_" + date + "_" + hour + "_" + minute + "_" + second;
		fileNameFarm = fileNameFarm + time + ".json";
		fileNameAgent = fileNameAgent + time + ".json";
		fileNameEnding = fileNameEnding + time + ".json";
		fileNameSales = fileNameSales + time + '_' + new Integer(salesFileCount).toString() + ".json";
		fileNameSupermarket = fileNameSupermarket + time + ".json";
		// f.createNewFile();
		File f = new File(fileNameFarm);
		f.createNewFile();
		f = new File(fileNameAgent);
		f.createNewFile();
		f = new File(fileNameEnding);
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
		endingFileWriter = new FileWriter(fileNameEnding);
	}

	public void recordData(IndexedIterable<? extends IAgent> agentList, int tick) throws IOException {
		if (agentList.size() == 0)
			return;

		ArrayList<farm> farms = new ArrayList<>();
		ArrayList<agent> agents = new ArrayList<>();
		ArrayList<supermarket> supermarkets = new ArrayList<>();
		IAgent t = agentList.get(0);
		if (t instanceof Farm) {
			for (int i = 0; i < agentList.size(); i++) {
				Farm x = (Farm) (agentList.get(i));
				farm f = new farm(tick, x);
				farms.add(f);
			}

		} else if (t instanceof Consumer) {
			for (int i = 0; i < agentList.size(); i++) {
				Consumer x = (Consumer) agentList.get(i);
				agent a = new agent(tick, x);
				agents.add(a);
			}
		} else if (t instanceof Supermarket) {

			for (int i = 0; i < agentList.size(); i++) {
				Supermarket S = (Supermarket) agentList.get(i);
				supermarket s = new supermarket(tick, S);
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

	public void recordSale(FoodOrder foodOrder, int tick, double income, String id, String consumerID)
			throws IOException {
		// System.out.println("salesFileCount:"+salesFileCount+"
		// "+tick+","+((tick/50)+1));
		if (tick / 50 + 1 > salesFileCount) {
			salesFileWriter.write("[]]");
			salesFileWriter.close();
			salesFileCount++;
			fileNameSales = "./output/Sales" + time + '_' + new Integer(salesFileCount).toString() + ".json";
			// System.out.println("switch sale file writer to " + fileNameSales);
			salesFileWriter = new FileWriter(fileNameSales);
			salesFileWriter.write('[');
		}
		saleRecord sr = new saleRecord(foodOrder, income, tick, id, consumerID);
		String salesJson = gson.toJson(sr);
		salesFileWriter.write(salesJson);
		salesFileWriter.write(',');

	}

	public void stopRecord() throws IOException {
		List<endingRecord> endingRecordList = new ArrayList<>();
		for (Farm f : AgentControl.getFarmAgents()) {
			endingRecordList.add(new endingRecord(f));

		}
		for (Supermarket s : AgentControl.getSupermarketAgents()) {
			endingRecordList.add(new endingRecord(s));
		}

		// System.out.println("total:" + dRecords.size());
		String endingJson = gson.toJson(endingRecordList);
		endingFileWriter.write(endingJson);
		endingFileWriter.close();
		agentFileWriter.write("[]]");
		agentFileWriter.close();
		farmFileWriter.write("[]]");
		farmFileWriter.close();
		supermarketFileWriter.write("[]]");
		supermarketFileWriter.close();
		endingFileWriter.close();
		salesFileWriter.write("[]]");
		salesFileWriter.close();
	}

	public static class farm {
		@Expose()
		String identifier;
		@Expose()
		int tick;
		// @Expose()
		// int stockNum;
		@Expose()
		HashMap<String, List<Food>> stock;
		// @Expose()
		// double count;
		@Expose()
		double fund;

		public farm(int t, Farm f) {
			tick = t;
			stock = f.getStock();
			// stockNum = f.getStock().size();
			// count = f.getCount();
			fund = f.getFund();
			try {
				identifier = f.getIdentifier();
			} catch (NoIdentifierException e) {
				e.printStackTrace();
			}

		}

		public farm() {
		}
	}

	public static class agent {
		@Expose()
		String id;
		@Expose()
		int tick;
		@Expose()
		double caloryConsumption;
		@Expose()
		double satisfaction;

		public agent(int t, Consumer a) {
			satisfaction = a.getAvgSatisfaction();
			id = a.toString();
			tick = t;
			caloryConsumption = a.getCaloryConsumption();
		}
	}

	public static class endingRecord {
		@Expose()
		String id;
		@Expose()
		HashMap<String, List<Waste>> waste;
		@Expose()
		double totalEnergyCost;
		@Expose()
		double totalCost;
		@Expose()
		double totalIncome;
		@Expose()
		double totalDiliveryCost;
		@Expose()
		double farmType;

		public endingRecord(SaleLocation l) {
			this.id = l.toString();
			waste = l.getWaste();

			if (l instanceof Farm) {
				this.totalCost = ((Farm) l).getTotalCost();
				this.totalEnergyCost = ((Farm) l).getTotalEnergyCost();
				this.totalIncome = ((Farm) l).getTotalIncome();
				this.totalDiliveryCost = ((Farm) l).getDiliveryCost();
				this.farmType = ((Farm) l).getFarmType();
			} else {
				this.totalCost = ((Supermarket) l).getTotalCost();
				this.totalEnergyCost = -1;
				this.totalIncome = ((Supermarket) l).getTotalIncome();
				this.totalDiliveryCost = -1;
				this.farmType = -1;
			}
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

		public saleRecord(FoodOrder foodOrder, double i, int t, String id, String cID) {
			identifier = id;
			consumerID = cID;
			income = i;
			tick = t;
			Set<Food> fSet = foodOrder.getList().keySet();
			order = new HashMap<String, Double>();
			java.util.Iterator<Food> iter = fSet.iterator();
			while (iter.hasNext()) {
				Food f = (Food) iter.next();
				Double val = foodOrder.getList().get(f);
				order.put(f.getSource() + "  " + f.getName(), val);
			}
		}
	}

	public static class supermarket {
		@Expose()
		String identifier;
		@Expose()
		int tick;
		// @Expose()
		// int stockNum;
		// @Expose()
		// HashMap<String,List<Food>> stock;
		// @Expose()
		// double count;
		@Expose()
		double StockLevel;
		@Expose()
		double fund;

		public supermarket(int t, Supermarket s) {
			this.tick = t;
			this.StockLevel = s.getStockCalorie();
			// stock = s.getStock();
			// stockNum = s.getStock().keySet().size();
			// count = f.getCount();
			fund = s.getFund();
			try {
				identifier = s.getIdentifier();
			} catch (NoIdentifierException e) {
				e.printStackTrace();
			}

		}

		public supermarket() {
		}

	}
}