package pl.cosie;

import java.util.Objects;
import java.util.Random;

public class Location {
    private int x;
    private int y;

    public Location(int x, int y) {
        this.x=x;
        this.y=y;
    }

    public Location copy() {
        return new Location(this.x, this.y);
    }

    public Location getRandNeighbourLocation() {
        Random random = new Random();
        Location location = new Location(this.x, this.y);
        while (location.x==this.x && location.y==this.y) {
            location.x = this.x;
            location.y = this.y;
            location.x += (random.nextInt(2)-1);
            location.y += (random.nextInt(2)-1);
        }
        return location;
    }
    public void randomizeNeighbourLocation(Location centre) {
        Random random = new Random();
        this.x = centre.x;
        this.y = centre.y;
        while (centre.x==this.x && centre.y==this.y) {
            this.x = centre.x;
            this.y = centre.y;
            this.x += (random.nextInt(2)-1);
            this.y += (random.nextInt(2)-1);

        }
    }
    public void randomizeInRange(Location leftLowerBound, Location rightUpperBound) {
        Random random = new Random();
        this.x = random.nextInt(rightUpperBound.x - leftLowerBound.x) + leftLowerBound.x;
        this.y = random.nextInt(rightUpperBound.y - leftLowerBound.y) + leftLowerBound.y;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return x == location.x &&
                y == location.y;
    }


    public String toString() {
        return "(" + this.x + ", " + this.y + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
