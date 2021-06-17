package cosie;

import java.util.Arrays;
import java.util.Random;

public class Genome {
    final static int gensNumber = 32;
    final static int gensKindsNumber = 8;

    private int[] gens;
    public Genome() {
        this.gens = new int[gensKindsNumber];
    }

    public Genome copy() {
        Genome copiedGenome = new Genome();
        System.arraycopy(this.gens, 0, copiedGenome.gens, 0, gensKindsNumber);
        return copiedGenome;
    }

    public void randomizeGenotype() {
        this.gens = new int[8];
        for (int i=0; i<gensKindsNumber; i++) {
            this.gens[i] = 1;
        }
        Random random = new Random();
        for (int i=0; i<(gensNumber*(gensKindsNumber-1))/gensKindsNumber; i += 1) {
            this.gens[random.nextInt(gensKindsNumber)] += 1;
        }
    }

    public void repairGenom() {
        Random random = new Random();
        while (Arrays.stream(this.gens).min().getAsInt()==0) {
            int to = 0;
            while (this.gens[to] != 0) {
                to += 1;
            }
            int from = random.nextInt(gensKindsNumber);
            while (this.gens[from] <= 1) {
                from = random.nextInt(gensKindsNumber);
            }
            this.gens[from] -= 1;
            this.gens[to] += 1;
        }
    }

    public int[] getGens() {
        return gens;
    }

    public Genome combineGenotypes(Genome genome) {
        Random random = new Random();
        int c1 = random.nextInt(gensNumber-1) + 1;
        int c2 = random.nextInt(gensNumber - (c1+1) + 1) + (c1+1);
        StringBuilder g1 = new StringBuilder();
        StringBuilder g2 = new StringBuilder();
        g1.append(this);
        g2.append(genome);
        boolean g2WasTaken = false;

        StringBuilder offspringGenStr = new StringBuilder();
        // Pierwsze lodowanie;
        if (random.nextInt(2)==0) {
            offspringGenStr.append(g1.subSequence(0, c1));
        } else {
            g2WasTaken = true;
            offspringGenStr.append(g2.subSequence(0, c1));
        }
        // Drugie losowanie;
        if (g2WasTaken) {
            offspringGenStr.append(g1.subSequence(c1, c2));
        } else {
            if (random.nextInt(2)==0) {
                offspringGenStr.append(g1.subSequence(c1, c2));
            } else {
                g2WasTaken = true;
                offspringGenStr.append(g2.subSequence(c1, c2));
            }
        }
        // Trzecie "losowanie";
        if (g2WasTaken) {
            offspringGenStr.append(g1.subSequence(c2, gensNumber));
        } else {
            offspringGenStr.append(g2.subSequence(c2, gensNumber));
        }

        Genome offspringGen = new Genome();
        for (int i=0; i<gensNumber; i++) {
            offspringGen.getGens()[offspringGenStr.codePointAt(i) - 48] += 1;
        }
        offspringGen.repairGenom();
        return offspringGen;
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        for (int g=0; g<gensKindsNumber; g++) {
            str.append(Integer.toString(g).repeat(this.gens[g]));
        }
        return str.toString();
    }

    public int getRotation() {
        Random random = new Random();
        int pos = random.nextInt(32);
        int g = 0;
        int s = 0;
        while (!(s <= pos && pos < s+this.gens[g])) {
            s += this.gens[g];
            g++;
        }
        return g;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Genome genome = (Genome) o;
        return Arrays.equals(gens, genome.gens);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(gens);
    }
}
