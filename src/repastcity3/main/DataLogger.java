package repastcity3.main;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class DataLogger {
	String name;
	int id;
	public DataLogger (String _name, int _id) {
		this.name = _name;
		this.id = _id;
	}
	public DataLogger () {
		this.name = "";
		this.id = 0;
	}
	public void printData() throws IOException {
		final String filePath = "./";
		final char RecordSeparator = '\n';
		CSVFormat formator = CSVFormat.DEFAULT.withRecordSeparator(RecordSeparator);
		FileWriter fileWriter = new FileWriter(filePath);
		CSVPrinter printer = new CSVPrinter(fileWriter, formator);

		DataLogger dl1 = new DataLogger("dl2",342);
		DataLogger dl2 = new DataLogger("dl2",342);
		ArrayList<DataLogger> list = new ArrayList<>();
		list.add(dl1);
		list.add(dl2);
		printer.printRecord(list);
		fileWriter.close();
		printer.close();
	
	
	}
	

}