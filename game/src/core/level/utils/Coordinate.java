package core.level.utils;

import core.utils.position.Position;

/** Coordinate in the dungeon, based on array index. */
public class Coordinate {

    public int x;
    public int y;

    /**
     * Create a new Coordinate
     *
     * @param x x-Coordinate
     * @param y y-Coordinate
     */
    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Create a new Coordinate from a given Position
     *
     * @param source Position to convert to Coordinate
     */
    public Coordinate(Position source) {
        this.x = (int) source.x;
        this.y = (int) source.y;
    }

    /**
     * Copy a coordinate
     *
     * @param copyFrom Coordinate to copy
     */
    public Coordinate(Coordinate copyFrom) {
        x = copyFrom.x;
        y = copyFrom.y;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Coordinate)) {
            return false;
        }
        Coordinate other = (Coordinate) o;
        return x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
        assert false : "hashCode nit designed";
        return x + y; // any arbitrary constant will do
    }

    /**
     * Convert Coordinate to Position
     *
     * @return
     */
    public Position toPoint() {
        return new Position(x, y);
    }
}
