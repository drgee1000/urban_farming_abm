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
import repastcity3.agent.People;
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

	Gson gson;
	Writer agentFileWriter;
	Writer farmFileWriter;

	public DataLogger() throws IOException {
		// RecordSeparator = '\n';
		// fileName1 = "./Output/farm_calorie_production";
		fileNameFarm = "./output/Farm";
		fileNameAgent = "./output/agent";
		// fileName4 = "./Output/agent_calorie_consumption";
		// fileName5 = "./Output/agent_health";

		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int date = c.get(Calendar.DATE);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int second = c.get(Calendar.SECOND);
		String time = year + "_" + month + "_" + date + "_" + hour + "_" + minute + "_" + second;
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

		gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
		agentFileWriter = new FileWriter(fileNameAgent);
		farmFileWriter = new FileWriter(fileNameFarm);
	}

	public DataLogger(char s) {
		RecordSeparator = s;
	}

	public <T> void recordData(IndexedIterable<T> agentList, int tick) throws IOException {
		if (agentList.size() == 0)
			return;

		ArrayList<farm> farms = new ArrayList<>();
		ArrayList<agent> agents = new ArrayList<>();
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
		}

		// printDataToJsonFile
		String farmJson = gson.toJson(farms);
		farmFileWriter.write(farmJson);
		String agentJson = gson.toJson(agents);
		agentFileWriter.write(agentJson);
	}

	public void stopRecord() throws IOException {
		agentFileWriter.close();
		farmFileWriter.close();
	}

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
		@Expose()
		double fund;

		public farm(int t, Farm f) {
			tick = t;
			stock = f.getStock();
			stockNum = stock.size();
			count = f.getCount();
			fund = f.getFund();
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