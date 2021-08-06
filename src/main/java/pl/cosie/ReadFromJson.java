package pl.cosie;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


public class ReadFromJson {

    final String fileName = "parameters.txt";
    int width;
    int height;
    double jungleRatio;
    double plantsOnStartRatio;
    int plantEnergy;
    int animalsOnStart;
    int moveEnergy;
    int startEnergy;
    int maxEnergy;
    double maxPlantDestructionRatio;

    public ReadFromJson() throws FileNotFoundException, ParseException {

        File file = new File(this.fileName);
        Scanner fileReader = new Scanner(file);
        JSONParser jsonParser = new JSONParser();
        StringBuilder s = new StringBuilder();

        while (fileReader.hasNext()){
            s.append(fileReader.next());
        }
        JSONObject obj = (JSONObject) jsonParser.parse(s.toString());

        this.height = ((Long) obj.get("height")).intValue();
        this.width = ((Long) obj.get("width")).intValue();
        this.jungleRatio = (Double) obj.get("jungleRatio");
        this.plantsOnStartRatio = (Double) obj.get("plantsOnStartRatio");
        this.animalsOnStart = ((Long) obj.get("animalsOnStart")).intValue();
        this.moveEnergy = ((Long) obj.get("moveEnergy")).intValue();
        this.startEnergy = ((Long) obj.get("startEnergy")).intValue();
        this.plantEnergy = ((Long) obj.get("plantEnergy")).intValue();
        this.maxEnergy = ((Long) obj.get("maxEnergy")).intValue();
        this.maxPlantDestructionRatio = (Double) obj.get("maxPlantDestructionRatio");
    }
}
