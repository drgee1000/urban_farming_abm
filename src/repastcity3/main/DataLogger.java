package repastcity3.main;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class DataLogger {
	public DataLogger () {
	}

	public void printData(ArrayList<String> agentList) throws IOException {
		System.out.println("start to export data to csv file");
		final String fileName = "data.csv";
		final char RecordSeparator = '\n';
		CSVFormat formator = CSVFormat.DEFAULT.withRecordSeparator(RecordSeparator);
		Writer fileWriter = new FileWriter(fileName);
		CSVPrinter printer = new CSVPrinter(fileWriter, formator);
		
		ArrayList<String> list = new ArrayList<>();
		String x;
		for (int i = 0; i < agentList.size(); i++) {
			x = agentList.get(i);
			list.add((x));
		}
		printer.printRecord(list);
		fileWriter.close();
		printer.close();
	
	
	}
	

}