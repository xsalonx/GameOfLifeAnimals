package cosie;

import java.util.ArrayList;


public class World {
    public final int width;
    public final int height;
    final double jungleRatio;

    public final int startEnergy;
    final int moveEnergy;
    final int plantEnergy;

    double maxPlantDestructionRatio;
    private final ArrayList<Statistics> statistics = new ArrayList<>();
    final Location leftLowerBound;
    final Location rightUpperBound;

    JungleField jungleField;
//    HashSet<Location> rocks;

    public World(ReadFromJson readFromJson) {
        this.width = readFromJson.width;
        this.height = readFromJson.height;
        this.jungleRatio = readFromJson.jungleRatio;
        this.startEnergy = readFromJson.startEnergy;
        this.moveEnergy = readFromJson.moveEnergy;
        this.plantEnergy = readFromJson.plantEnergy;
        this.leftLowerBound = new Location(0, 0);
        this.rightUpperBound = new Location(width-1, height -1);
        this.jungleField = new JungleField(width, height, jungleRatio);
        this.maxPlantDestructionRatio = readFromJson.maxPlantDestructionRatio;
    }

    WorldEvolution letThereBeALive(int animalsOnStart, double plantsRatioOnStart){

        return null;
    }




}
