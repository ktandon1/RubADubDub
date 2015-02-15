public class Patch {
    private double [][][] data;
    private int x; // patch coordinate in "patch image"
    private int y; // patch coordinate in "patch image"
    public Patch(double[][][] data, int x, int y) {
        this.data = data;
        this.x = x;
        this.y = y;
    }

    public Patch(double[][][] data) {
        this(data, 0, 0);
    }
    public double[][][] getData() {
        return this.data;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

}