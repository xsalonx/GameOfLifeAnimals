package pl.game.cosie;

import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

public interface WorldEvolution {

    void letDayGoBy() throws Exception;
    int getDay();
    int getLeavingAnimalsNumber();
    void startObservation();
    ObservedAncestor getObservedAncestor();
    void stopObserving();
    String toString();
    Statistics getStatistics();

    int getWidth();
    int getHeight();
    HashSet<Location> getPlants();
    HashMap<Location, PriorityQueue<Animal>> getAnimalsInLocations();
    WorldEvolution copyWithSameWorld();
    void restart();
    boolean isStopped();
    void stop();
    void run();
    void copyPlantsAndAnimals(WorldEvolution worldEvolution);
    double getAvgHeathLevel();
    double getAvgLifeLength();
    double getAvgChildrenNumberOfLeaving();
    Genome getCurrMostPopularGenome();
    HashMap<Genome, Integer> getCurrGenomesOccurence();



}
