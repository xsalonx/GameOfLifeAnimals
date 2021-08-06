package pl.cosie;

public class JungleField {

    Location leftLowerBound;
    Location rightUpperBound;

    JungleField(int width, int length, double jungleRatio) {
        double p = Math.sqrt(jungleRatio);
        this.leftLowerBound = new Location((int)(width*(1-p)/2), (int)(length*(1-p)/2));
        this.rightUpperBound = new Location((int)(width*(1+p)/2), (int)(length*(1+p)/2));
    }

    boolean containLocation(Location location) {
        return this.leftLowerBound.getX() <= location.getX() && location.getX() <= this.rightUpperBound.getX() &&
                this.leftLowerBound.getY() <= location.getY() && location.getY() <= this.rightUpperBound.getY();
    }

}
