package level.tools;

public class Coordinate {

    public int x;
    public int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Coordinate(Coordinate c) {
        x = c.x;
        y = c.y;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Coordinate)) return false;
        Coordinate other = (Coordinate) o;
        return x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
        assert false : "hashCode nit designed";
        return x + y; // any arbitrary constant will do
    }
}
