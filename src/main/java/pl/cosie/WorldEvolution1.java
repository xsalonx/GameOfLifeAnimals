package pl.cosie;


import java.util.*;

public class WorldEvolution1 implements WorldEvolution{
    static final int maxAnimalsOnOneFiled=150;
    static final double ratioAfterExterminationDueToOverpopulation = 0.95;
    static int maxRandsAttempts=100;
    static int maxPlantsOnStartRandsAttempts=100000;

    HashSet<Location> overpopulatedFields = new HashSet<>();
    World world;
    public int day = 0;
    HashSet<Location> plants = new HashSet<>();
    HashMap<Location, PriorityQueue<Animal>> animalsInLocation = new HashMap<>();
    HashMap<Location, PriorityQueue<Animal>> animalsInLocationAfterMove = new HashMap<>();
    ObservedAncestor observedAncestor;
    ArrayList<Animal> entombedAnimals = new ArrayList<>();
    HashMap<Genome, Integer> currGenomesOccurrings = new HashMap<>();
    HashMap<Genome, Integer> genomesOccurrings = new HashMap<>();
    Genome winingGenome;
    public int leavingAnimalsNumber=0;

    public boolean stopped = true;

    private ReadFromJson readFromJson;

    private void implyJsonData(ReadFromJson readFromJson) {
        if (readFromJson != null) {
            this.readFromJson = readFromJson;
            Animal.setStartMaxEnergy(readFromJson.maxEnergy);
            ArrayList<Animal> AdamsAndEves = new ArrayList<>();
            Location loc0 = new Location(0, 0);
            Location locUpR = new Location(world.width, world.height);
            for (int i = 0; i < readFromJson.animalsOnStart; i++) {
                Genome g = new Genome();
                g.randomizeGenotype();
                Location l = new Location(0, 0);
                l.randomizeInRange(loc0, locUpR);
                Animal a = new Animal("", world.startEnergy, g, l, 0);
                AdamsAndEves.add(a);
            }
            for (Animal animal : AdamsAndEves) {
                PriorityQueue<Animal> pq = this.animalsInLocation.getOrDefault(animal.getLocation(), new PriorityQueue<>());
                pq.add(animal);
                this.animalsInLocation.put(animal.getLocation(), pq);
                this.leavingAnimalsNumber += 1;
                this.currGenomesOccurrings.put(animal.genome, this.currGenomesOccurrings.getOrDefault(animal.genome, 0) + 1);
                this.genomesOccurrings.put(animal.genome, this.genomesOccurrings.getOrDefault(animal.genome, 0) + 1);

            }

            int reqPlantsNumber = (int) (readFromJson.plantsOnStartRatio * world.height * world.width);
            int failureAttemptsToAddPlant = 0;
            while (plants.size() < reqPlantsNumber && failureAttemptsToAddPlant < maxPlantsOnStartRandsAttempts) {
                int attemptsOfRandaringPlant = 0;
                Location plant = new Location(0, 0);
                plant.randomizeInRange(this.world.leftLowerBound, this.world.rightUpperBound);
                while (this.animalsInLocation.containsKey(plant) && this.plants.contains(plant) && attemptsOfRandaringPlant < maxPlantsOnStartRandsAttempts) {
                    plant.randomizeInRange(this.world.leftLowerBound, this.world.rightUpperBound);
                    attemptsOfRandaringPlant += 1;
                }
                if (!this.animalsInLocation.containsKey(plant) && !this.plants.contains(plant)) {
                    this.plants.add(plant);
                } else {
                    failureAttemptsToAddPlant += 1;
                }
            }
        }
    }

    public WorldEvolution1(World world, ReadFromJson readFromJson){
        this.world = world;

        this.implyJsonData(readFromJson);


    }

    public boolean isLocationAllowedToMoveOn(Location location) {
        return 0 <= location.getX() && location.getX() < this.world.width && 0 <= location.getY() && location.getY() < this.world.height;
    }

    private void removeDeadAnimals() throws Exception {
        for (Location location : this.animalsInLocation.keySet()) {
            PriorityQueue<Animal> pq = this.animalsInLocation.get(location);
            while (!pq.isEmpty()) {
                Animal animal = pq.poll();
                animal.energy=0;
                animal.dayOfDeath = this.day;
                this.entombedAnimals.add(animal);
                this.currGenomesOccurrings.put(animal.genome, this.currGenomesOccurrings.getOrDefault(animal.genome, 0) - 1);
                if (this.currGenomesOccurrings.get(animal.genome) == 0) {
                    this.currGenomesOccurrings.remove(animal.genome);
                }
                this.leavingAnimalsNumber -= 1;
            }
        }
        this.animalsInLocation.clear();
    }

    private void moveAnimals(PriorityQueue<Animal> pq, int moveEnergy) throws Exception {
        for (Animal animal : pq.toArray(new Animal[0])) {
            Location animalDestination = animal.animalIsGoingTo();
            while (!this.isLocationAllowedToMoveOn(animalDestination)) {
                animalDestination = animal.animalIsGoingTo();
            }
            if (!(animal.energy < this.world.moveEnergy || (!this.plants.contains(animalDestination) && animal.energy < 2*this.world.moveEnergy))) {

                PriorityQueue<Animal> pqAfterMove = this.animalsInLocationAfterMove.getOrDefault(animalDestination, new PriorityQueue<>());
                animal.changeLocation(animalDestination);
                pqAfterMove.add(animal);
                pq.remove(animal);
                animal.energy -= moveEnergy;
                this.animalsInLocationAfterMove.put(animalDestination, pqAfterMove);
                if (pqAfterMove.size() > maxAnimalsOnOneFiled) {
                    this.overpopulatedFields.add(animalDestination);
                }
            }

        }
    }

    private void feedAnimals(PriorityQueue<Animal> pq) throws Exception {
        int beingEatenPlantEnergy = this.world.plantEnergy;
        HashSet<Animal> fedAnimals = new HashSet<>();
        int eatenEnergy;
        int posEnergy;
        HashSet<Animal> beingFedAnimals = new HashSet<>();
        while (beingEatenPlantEnergy  > 0 && !pq.isEmpty()) {

            int strongestAnimalEnergy = pq.peek().energy;
            while (!pq.isEmpty() && pq.peek().energy == strongestAnimalEnergy) {
                beingFedAnimals.add(pq.poll());
            }
            posEnergy = beingEatenPlantEnergy / beingFedAnimals.size();
            eatenEnergy = Math.min(Animal.maxEnergy - strongestAnimalEnergy, posEnergy);

            if (eatenEnergy > 0) {
                for (Animal animal : beingFedAnimals) {

                    animal.energy += eatenEnergy;
                    beingEatenPlantEnergy -= eatenEnergy;
                    fedAnimals.add(animal);
                }
                beingFedAnimals.clear();
            } else {
                beingEatenPlantEnergy = 0;
            }
        }

        pq.addAll(beingFedAnimals);
        pq.addAll(fedAnimals);

        if ((double)beingEatenPlantEnergy / (double)this.world.plantEnergy > this.world.maxPlantDestructionRatio) {
            assert pq.peek() != null;
            this.plants.remove(pq.peek().location);
        }
    }

    private void pairAnimals(PriorityQueue<Animal> pq) throws Exception {
        if (pq.size() < 2) return;
        Animal animal1 = pq.poll();
        Animal animal2 = pq.poll();
        int attempts = 0;
        assert animal2 != null;
        if (animal2.canGoOnDate(this.world.startEnergy/2)) {
            Animal offspring = animal1.pairWith(animal2, this.day);
            while (!this.isLocationAllowedToMoveOn(offspring.location) && attempts < maxRandsAttempts) {
                offspring.location.randomizeNeighbourLocation(animal1.location);
                attempts += 1;
            }
            if (attempts == maxRandsAttempts) {
                offspring = null;
                return;
            }
            PriorityQueue<Animal> offspringPQ = this.animalsInLocation.getOrDefault(offspring.location, new PriorityQueue<>());
            offspringPQ.add(offspring);
            if (offspringPQ.size() > maxAnimalsOnOneFiled) {
                this.overpopulatedFields.add(offspring.location);
            }
            this.currGenomesOccurrings.put(offspring.genome, this.currGenomesOccurrings.getOrDefault(offspring.genome, 0) + 1);
            this.genomesOccurrings.put(offspring.genome, this.genomesOccurrings.getOrDefault(offspring.genome, 0) + 1);
            animal1.numberOfChildren += 1;
            animal2.numberOfChildren += 1;
            ObservedAncestor ancestor;
            if (animal1.ancestorUnderObservation != null) {
                ancestor = animal1.ancestorUnderObservation;
            } else {
                ancestor = animal2.ancestorUnderObservation;
            }
            if (ancestor != null) {
                ancestor.currNumberOfDescendants += 1;
                offspring.ancestorUnderObservation = ancestor;
                ancestor.descendantsInNthDay.put(this.day, ancestor.currNumberOfDescendants);
                if (ancestor.ancestor == animal1 || ancestor.ancestor == animal2) {
                    ancestor.childrenInNthDay.put(this.day, ancestor.childrenInNthDay.getOrDefault(this.day, ancestor.ancestor.numberOfChildren));
                }
            }
            this.animalsInLocation.put(offspring.location, offspringPQ);
            this.leavingAnimalsNumber += 1;
        }
        pq.add(animal1);
        pq.add(animal2);
    }

    private void thinOverpopulatedFields() {


        for (Location location : this.overpopulatedFields) {
            PriorityQueue<Animal> PQAfterExtermination = new PriorityQueue<>();
            PriorityQueue<Animal> currPQ = this.animalsInLocation.get(location);
            for (int i=0; i<maxAnimalsOnOneFiled * ratioAfterExterminationDueToOverpopulation; i++) {
                PQAfterExtermination.add(currPQ.poll());
            }
            for (Animal animal : currPQ) {
                this.entombedAnimals.add(animal);
                animal.dayOfDeath = this.day;
                this.leavingAnimalsNumber -= 1;
            }
            currPQ.clear();
            this.animalsInLocation.put(location, PQAfterExtermination);
        }

    }

    public void growNewPlants() {
        Location plant1 = new Location(0, 0);
        Location plant2 = new Location(0, 0);
        int attempts = 0;
        plant1.randomizeInRange(this.world.jungleField.leftLowerBound, this.world.jungleField.rightUpperBound);
        while (attempts < maxRandsAttempts && (this.animalsInLocation.containsKey(plant1) || this.plants.contains(plant1))) {
            plant1.randomizeInRange(this.world.jungleField.leftLowerBound, this.world.jungleField.rightUpperBound);
            attempts += 1;
        }
        Location loc0 = new Location(0,0);
        Location locUpR =  new Location(this.world.width, world.height);
        plant2.randomizeInRange(loc0, locUpR);
        attempts = 0;
        while (attempts < maxRandsAttempts && ((this.world.jungleField.containLocation(plant2) && this.animalsInLocation.containsKey(plant2)) || this.plants.contains(plant1))){
            plant2.randomizeInRange(loc0, locUpR);
            attempts += 1;
        }

        this.plants.add(plant1);
        this.plants.add(plant2);
    }



    @Override
    public void letDayGoBy() throws Exception {
        for (Location key : this.animalsInLocation.keySet()) {
            this.moveAnimals(this.animalsInLocation.get(key), this.world.moveEnergy);
        }
        HashMap<Location, PriorityQueue<Animal>> tmp;
// Usuwanie martwych zwierzątek
        this.removeDeadAnimals();
        tmp = this.animalsInLocation;
        this.animalsInLocation = this.animalsInLocationAfterMove;
        this.animalsInLocationAfterMove = tmp;

// Jedzenie i rozmnażanie
        for (Location location : this.animalsInLocation.keySet().toArray(new Location[0])) {
            if (this.plants.contains(location)) {
                this.feedAnimals(this.animalsInLocation.get(location));
            }

            this.pairAnimals(this.animalsInLocation.get(location));
        }


// Wyrastanie nowych roślinek
        this.growNewPlants();

        this.thinOverpopulatedFields();
        this.day += 1;

        if (this.winingGenome == null && this.currGenomesOccurrings.size() == 1) {

            Object[] a;
            a = this.currGenomesOccurrings.keySet().toArray();
            this.winingGenome = (Genome)a[0];
        }
    }

    @Override
    public int getLeavingAnimalsNumber(){
        return this.leavingAnimalsNumber;
    }

    @Override
    public void startObservation() {
        Animal ancestor = null;
        int maxEnergy = 0;
        for (PriorityQueue<Animal> pq : this.animalsInLocation.values()){
            for (Animal animal : pq) {
                if (animal.getEnergy() > maxEnergy) {
                    ancestor = animal;
                    maxEnergy = animal.getEnergy();
                }
            }
        }
        if (ancestor == null) {
            return;
        }
        this.observedAncestor = new ObservedAncestor(ancestor, this.day);
        ancestor.name = "Darek";
    }

    @Override
    public ObservedAncestor getObservedAncestor() {
        return this.observedAncestor;
    }

    @Override
    public void stopObserving() {
        this.observedAncestor = null;
        for (PriorityQueue<Animal> pq : this.animalsInLocation.values()) {
            for (Animal animal : pq) {
                animal.ancestorUnderObservation = null;
            }
        }
    }


    public Genome getCurrMostFrequentGenome() {
        Genome genome = null;
        int occ = 0;
        for (Genome g : this.currGenomesOccurrings.keySet()) {
            if (this.currGenomesOccurrings.get(g) > occ) {
                genome = g;
            }
        }
        return genome;
    }

    public Genome getMostFrequentGenome() {
        Genome genome = null;
        int occ = 0;
        for (Genome g : this.genomesOccurrings.keySet()) {
            if (this.genomesOccurrings.get(g) > occ) {
                genome = g;
            }
        }
        return genome;
    }







    @Override
    public int getDay() {
        return this.day;
    }

    @Override
    public String toString() {
        return "day: " + this.day + ";; animals: " + this.leavingAnimalsNumber + ";; plants number: " + this.plants.size() + ";; number of entombed animals: " + this.entombedAnimals.size() + "\n      different genoms: " + this.currGenomesOccurrings.size() + ";; diff genomes during whole simulation: " + this.genomesOccurrings.size();
    }

    @Override
    public Statistics getStatistics() {
        Statistics statistics = new Statistics();


        return statistics;
    }


    @Override
    public int getWidth() {
        return this.world.width;
    }
    @Override
    public int getHeight() {
        return this.world.height;
    }

    @Override
    public HashSet<Location> getPlants() {
        return this.plants;
    }

    @Override
    public HashMap<Location, PriorityQueue<Animal>> getAnimalsInLocations() {
        return this.animalsInLocation;
    }

    @Override
    public WorldEvolution copyWithSameWorld() {
        WorldEvolution1 copy = new WorldEvolution1(this.world, null);
        copy.readFromJson = this.readFromJson;
        // Plants coping
        for (Location plant : this.plants){
            copy.getPlants().add(plant);
        }
        // Animals coping
        for (Location loc : this.animalsInLocation.keySet()) {
            PriorityQueue<Animal> pq = this.animalsInLocation.get(loc);
            copy.animalsInLocation.put(loc, new PriorityQueue<>());
            for (Animal animal : pq) {
                copy.animalsInLocation.get(loc).add(animal.copy());
            }
        }
        // Genomes coping
        for (Map.Entry<Genome, Integer> entry : this.genomesOccurrings.entrySet()) {
            copy.genomesOccurrings.put(entry.getKey().copy(), entry.getValue());
        }
        for (Map.Entry<Genome, Integer> entry : this.currGenomesOccurrings.entrySet()) {
            copy.currGenomesOccurrings.put(entry.getKey().copy(), entry.getValue());
        }
        return copy;
    }

    private void cleanWorld() {
        this.currGenomesOccurrings.clear();
        this.genomesOccurrings.clear();
        this.animalsInLocation.clear();
        this.animalsInLocationAfterMove.clear();
        this.plants.clear();
        this.entombedAnimals.clear();
        this.day = 0;
        this.observedAncestor = null;
        this.winingGenome = null;
        this.leavingAnimalsNumber = 0;
        this.overpopulatedFields.clear();
    }

    @Override
    public void restart() {

        this.cleanWorld();
        this.implyJsonData(this.readFromJson);
    }

    @Override
    public boolean isStopped() {
        return this.stopped;
    }

    @Override
    public void stop() {
        this.stopped = true;
    }

    @Override
    public void run() {
        this.stopped = false;
    }

    @Override
    public void copyPlantsAndAnimals(WorldEvolution worldEvolution) {
        this.cleanWorld();
        for (Map.Entry<Location, PriorityQueue<Animal>> entry : worldEvolution.getAnimalsInLocations().entrySet()) {
            this.animalsInLocation.put(entry.getKey(), new PriorityQueue<>());
            for (Animal animal : entry.getValue()){
                this.animalsInLocation.get(entry.getKey()).add(animal.copy());
                this.leavingAnimalsNumber += 1;
                this.currGenomesOccurrings.put(animal.genome, this.currGenomesOccurrings.getOrDefault(animal.genome, 0) + 1);
                this.genomesOccurrings.put(animal.genome, this.genomesOccurrings.getOrDefault(animal.genome, 0) + 1);

            }
        }
        for (Location plant : worldEvolution.getPlants()) {
            this.plants.add(plant.copy());
        }
        this.leavingAnimalsNumber = worldEvolution.getLeavingAnimalsNumber();

    }

    @Override
    public double getAvgHeathLevel() {
        long totalHP = 0;
        for (PriorityQueue<Animal> pq : this.animalsInLocation.values()) {
            for (Animal animal : pq) {
                totalHP += animal.getEnergy();
            }
        }
        if (this.leavingAnimalsNumber == 0) return -1;
        return (double) totalHP/ (double) this.leavingAnimalsNumber;
    }

    @Override
    public double getAvgLifeLength() {
        long totalLivedOnDays = 0;
        for (Animal animal : this.entombedAnimals) {
            totalLivedOnDays += (animal.dayOfDeath - animal.birthday);
        }
        if (this.entombedAnimals.size() == 0) return -1;
        return (double) totalLivedOnDays / (double) this.entombedAnimals.size();
    }

    @Override
    public double getAvgChildrenNumberOfLeaving() {
        long totalChildrenNumber = 0;
        for (PriorityQueue<Animal> pq : this.animalsInLocation.values()) {
            for (Animal animal : pq) {
                totalChildrenNumber += animal.numberOfChildren;
            }
        }
        if (this.leavingAnimalsNumber == 0) return -1;
        return (double) totalChildrenNumber/ (double) this.leavingAnimalsNumber;
    }

    @Override
    public Genome getCurrMostPopularGenome() {
        Genome best = null;
        int a = 0;
        for (Map.Entry<Genome, Integer> entry : this.currGenomesOccurrings.entrySet()) {
            if (entry.getValue() > a) {
                best = entry.getKey();
            }
        }
        return best;
    }

    @Override
    public HashMap<Genome, Integer> getCurrGenomesOccurence() {
        return this.currGenomesOccurrings;
    }

}
