package repastcity3.environment.food;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;

public class FoodClassifier {
    ArrayList<String> grain_list;
    ArrayList<String> vegetable_list;
    ArrayList<String> fruit_list;
    ArrayList<String> meat_list;
    ArrayList<String> dairy_list;

    public FoodClassifier(){
        grain_list = new ArrayList<>();
        vegetable_list = new ArrayList<>();
        fruit_list = new ArrayList<>();
        meat_list = new ArrayList<>();
        dairy_list = new ArrayList<>();
        try (Reader in = new FileReader("./data/food_data/food.csv")){
            CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader();
            Iterable<CSVRecord> records = format.parse(in);
            for (CSVRecord record : records) {
                String name = record.get("name");
                String type = record.get("type");
                switch (type){
                    case "grain":
                        grain_list.add(name);
                        break;
                    case "fruit":
                        fruit_list.add(name);
                        break;
                    case "vegetable":
                        vegetable_list.add(name);
                        break;
                    case "dairy":
                        dairy_list.add(name);
                        break;
                    case "meat":
                    	meat_list.add(name);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ArrayList<String> getDairy_list() {
        return dairy_list;
    }

    public ArrayList<String> getFruit_list() {
        return fruit_list;
    }

    public ArrayList<String> getGrain_list() {
        return grain_list;
    }

    public ArrayList<String> getMeat_list() {
        return meat_list;
    }

    public ArrayList<String> getVegetable_list() {
        return vegetable_list;
    }
}

