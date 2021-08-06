package pl.cosie;

import java.util.Random;

public class Animal implements Comparable<Animal>{

    public static int maxEnergy;
    static int[] xMoveTable = new int[]{0, 1, 1, 1, 0, -1, -1, -1};
    static int[] yMoveTable = new int[]{1, 1, 0, -1, -1, -1, 0, 1};
    final static int numberOfDirections = 8;

    public String name;
    int energy;
    Location location;
    int currentDirection;
    public int numberOfChildren = 0;

    public ObservedAncestor getAncestorUnderObservation() {
        return ancestorUnderObservation;
    }

    ObservedAncestor ancestorUnderObservation = null;

    int birthday;
    public int dayOfDeath;
    final Genome genome;

    public static void setStartMaxEnergy(int maxEnergy) {
        Animal.maxEnergy = maxEnergy;
    }

    public Animal(String name, int energy, Genome genome, Location location, int birthday) {
        this.name = name;
        this.energy = Math.min(energy, maxEnergy);
        Random random = new Random();
        this.currentDirection = random.nextInt(numberOfDirections);
        this.genome = genome;
        this.location = location;
        this.birthday = birthday;
    }

    public Animal copy() {
       return new Animal(this.name, this.energy, this.genome.copy(), this.location.copy(), this.birthday);
    }

    public Location getLocation() {
        return this.location;
    }

    public int getEnergy() {
        return this.energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public Genome getGenotype() {
        return genome;
    }

    public void changeLocation(Location location) {
        this.location = location;
    }

    public void changeDirection() {
        int rotation = this.genome.getRotation();
        this.currentDirection = (this.currentDirection + rotation) % numberOfDirections;
    }

    public boolean canGoOnDate(int minEnergy) {
        return this.energy >= minEnergy;
    }

    public Animal pairWith(Animal animal, int day){

        Genome offspringGenome = this.genome.combineGenotypes(animal.getGenotype());
        int energy1 = this.energy/4;
        int energy2 = animal.energy/4;
        this.energy -= energy1;
        animal.energy -= energy2;
        return new Animal("", energy1+energy2, offspringGenome, this.location.getRandNeighbourLocation(), day);
    }

    public Location animalIsGoingTo() {
        this.changeDirection();
        return new Location(this.location.getX() + xMoveTable[this.currentDirection],
                this.location.getY() + yMoveTable[this.currentDirection]);
    }


    @Override
    public int compareTo(Animal o) {
        return -(this.energy - o.energy);
    }
}
